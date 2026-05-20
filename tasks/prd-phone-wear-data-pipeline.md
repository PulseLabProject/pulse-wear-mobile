# PRD: Phone–Wear OS Data Pipeline

## Introduction

Establish a real-time data pipeline from the Wear OS watch to the phone companion app. The watch collects heart rate, HRV, SpO2, and stress index, transmits them to the phone via the Wearable Data Layer API (`MessageClient`), and the phone both displays the live readings in `StatusScreen` and relays each reading to the Node+Postgres backend. This replaces the current architecture where the watch posted to the backend directly.

**Recommended channel:** `MessageClient` (not `DataClient`). It is fire-and-forget, low-latency, and suited for continuous sensor streaming. `DataClient` syncs state — appropriate for config or user preferences, not for a stream of readings.

## Goals

- Replace all hardcoded values in `StatusScreen` with live sensor readings from the watch
- Establish real-time watch → phone communication using `MessageClient`
- Phone app becomes the single relay point to the Node+Postgres backend (watch stops posting directly)
- All 4 health metrics (HR, HRV, SpO2, stress index) flow end-to-end through the pipeline

## User Stories

### US-001: Add Wearable Data Layer dependency to the phone module
**Description:** As a developer, I need `play-services-wearable` in the `:app` module so the phone can participate in the Wearable Data Layer.

**Acceptance Criteria:**
- [ ] `play-services-wearable` added to `app/build.gradle.kts` via `libs.versions.toml` entry
- [ ] Verify `play-services-wearable` is also present in `wear/build.gradle.kts` (it is likely already declared)
- [ ] `./gradlew :app:assembleDebug` passes with no errors

---

### US-002: Define shared sensor data model and message protocol
**Description:** As a developer, I need a consistent data structure and message path constants so the watch sender and phone receiver speak the same protocol without a shared Gradle module.

**Acceptance Criteria:**
- [ ] `SensorBundle` data class created in `wear/model/` with fields: `bpm: Int`, `hrv: Int`, `spo2: Int`, `stressIndex: Int`, `timestamp: Long`
- [ ] `MessagePaths` singleton object created in `wear/` with `const val SENSOR_DATA = "/sensor/data"`
- [ ] Equivalent `SensorBundle` and `MessagePaths` created in `app/` (JSON is the contract; no shared module)
- [ ] `SensorBundle` serializes to/from JSON using Gson (already a transitive dependency via Retrofit)
- [ ] `./gradlew :wear:assembleDebug` and `./gradlew :app:assembleDebug` both pass

---

### US-003: Extend wear data collection to all 4 metrics
**Description:** As a developer, I need `HeartRateRepository` to provide HRV, SpO2, and stress index alongside BPM so a complete `SensorBundle` can be assembled on each tick.

**Acceptance Criteria:**
- [ ] `HeartRateReading` model updated to include `hrv: Int`, `spo2: Int`, `stressIndex: Int` fields alongside the existing `bpm`
- [ ] Simulation mode generates plausible values: HRV 30–80 ms, SpO2 95–100%, stress 0–100
- [ ] `HomeViewModel` state carries all 4 values (update `HomeUiState.Monitoring` or equivalent)
- [ ] `HomeScreen` on the watch compiles and does not crash after the model extension
- [ ] `./gradlew :wear:assembleDebug` passes

---

### US-004: Send SensorBundle from watch to phone via MessageClient
**Description:** As a developer, I need the watch to transmit each reading to the phone in real time so the phone can display and relay it.

**Acceptance Criteria:**
- [ ] `WearDataSender` created in `:wear` (a plain class or `object`), uses `Wearable.getMessageClient(context)`
- [ ] On each new reading in `HomeViewModel`, a Gson-encoded `SensorBundle` (as `ByteArray`) is sent to all connected phone nodes at path `/sensor/data`
- [ ] Node discovery uses `Wearable.getNodeClient(context).connectedNodes` — iterates all results
- [ ] No-node or send failure is caught silently (coroutine `try/catch`) — no crash, no UI disruption
- [ ] Existing watch-to-backend direct API calls in `ApiRepository` / `HomeViewModel` are removed or disabled to avoid duplicate POSTs
- [ ] `./gradlew :wear:assembleDebug` passes

---

### US-005: Create WearableListenerService on the phone
**Description:** As a developer, I need a background service on the phone that receives messages from the watch and exposes them to the UI layer without coupling the service to the ViewModel.

**Acceptance Criteria:**
- [ ] `WearListenerService : WearableListenerService` created in `:app` under `com.pl.pulselab`
- [ ] `onMessageReceived` checks that the path equals `/sensor/data`, then decodes the byte payload via Gson into `SensorBundle`
- [ ] Decoded bundle is emitted to a `MutableStateFlow<SensorBundle?>` held in a `SensorDataStore` Kotlin `object` singleton (bridges Service lifecycle ↔ ViewModel lifecycle)
- [ ] Service declared in `app/AndroidManifest.xml` with `<intent-filter>` containing `<action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />`
- [ ] `./gradlew :app:assembleDebug` passes

---

### US-006: Create StatusViewModel and wire StatusScreen to live data
**Description:** As a researcher using the phone app, I want the displayed readings to update in real time as data arrives from the watch, replacing the current hardcoded values.

**Acceptance Criteria:**
- [ ] `StatusViewModel` created in `:app`, collects from `SensorDataStore.sensorFlow` and exposes a `StateFlow<SensorBundle?>`
- [ ] `StatusScreen` refactored into stateless `StatusScreenContent(bundle: SensorBundle?, lastSyncTime: String?)` + stateful `StatusScreen(viewModel: StatusViewModel)` wrapper — following the existing UI split pattern from `HomeScreen`
- [ ] All 4 metric cards (HR, HRV, SpO2, stress) show live values from the ViewModel
- [ ] When `bundle` is `null` (no data received yet), each value card displays `"--"` as the value
- [ ] `./gradlew :app:assembleDebug` passes

---

### US-007: Add Retrofit + OkHttp to phone module and build ApiRepository
**Description:** As a developer, I need the phone app to have its own network layer so it can relay each received reading to the Node+Postgres backend.

**Acceptance Criteria:**
- [ ] Retrofit and `converter-gson` added to `app/build.gradle.kts` (mirror the `:wear` declarations)
- [ ] `ApiService` interface created in `app/data/network/` with a `@POST` endpoint accepting `SensorBundle` as the request body
- [ ] `ApiRepository` created in `app/data/network/` with `BASE_URL` placeholder constant and an OkHttp auth header interceptor (token as a `TODO` constant for now)
- [ ] `StatusViewModel` calls `apiRepository.postReading(bundle)` on each non-null bundle, on `Dispatchers.IO`
- [ ] Network failure is caught and exposed as a `syncError: String?` in the ViewModel state — does not crash the app
- [ ] `./gradlew :app:assembleDebug` passes

---

### US-008: Reflect sync status in StatusScreen
**Description:** As a researcher, I want to see whether the last relay to the backend succeeded so I know the data is being persisted.

**Acceptance Criteria:**
- [ ] "Estado do Servidor" card in `StatusScreen` shows `"Online"` (green) on successful POST, `"Erro"` (red) on failure
- [ ] "Último Envio" row shows the timestamp of the last successful POST (formatted as `"Hoje às HH:mm"`)
- [ ] "Sincronização" card spins while a POST is in flight; shows `"Concluído"` or `"Falhou"` after it resolves
- [ ] All status strings are driven by `StatusViewModel` state, not hardcoded
- [ ] `./gradlew :app:assembleDebug` passes

---

## Functional Requirements

- FR-1: `:app` must declare `play-services-wearable` as a dependency
- FR-2: `SensorBundle` must carry `bpm`, `hrv`, `spo2`, `stressIndex`, `timestamp` in both modules
- FR-3: Message path `/sensor/data` must be identical in `WearDataSender` and `WearListenerService`
- FR-4: Watch simulation mode must produce plausible values for all 4 metrics on every tick
- FR-5: `WearListenerService` must be registered in `AndroidManifest.xml` with the `MESSAGE_RECEIVED` action — the Wearable runtime starts it automatically; do not start it manually
- FR-6: `SensorDataStore` must be a Kotlin `object` holding a `MutableStateFlow<SensorBundle?>` initialised to `null`
- FR-7: `StatusScreen` must contain no hardcoded sensor values — all values come from `StatusViewModel`
- FR-8: The watch must no longer POST to the backend directly — all backend writes go through the phone
- FR-9: Network failure on the phone relay must not crash the app or block the UI thread

## Non-Goals

- No Bluetooth pairing UI — assumes watch and phone are already paired via Wear OS companion app
- No offline read buffering / retry queue on the phone (future story)
- No user authentication or token management beyond a placeholder constant (handled in a future auth story)
- No changes to `ConsentActivity` or the consent/onboarding flow
- No shared Gradle module between `:app` and `:wear` — JSON is the contract, both modules own their own data classes
- No changes to the watch's `HomeScreen` UI beyond what's required to compile after the model extension

## Technical Considerations

- **Node discovery:** `Wearable.getNodeClient(context).connectedNodes.await()` returns a `Task`; use `.await()` from `kotlinx-coroutines-play-services` or wrap in `suspendCoroutine`
- **`SensorDataStore`:** A Kotlin `object` singleton is the standard bridge between a `Service` (which Android controls) and a `ViewModel` (which the UI controls). `StateFlow` is preferred over `SharedFlow` here so that screen rotation does not blank out the last reading
- **Retrofit in `:app`:** Mirror the exact Retrofit setup from `:wear` — same `converter-gson`, same OkHttp interceptor pattern; keep `BASE_URL` as a `const val TODO` until the backend URL is confirmed
- **Coroutine scope in Service:** Use `CoroutineScope(SupervisorJob() + Dispatchers.IO)` inside `WearListenerService`; cancel it in `onDestroy`
- **Gson dependency in `:app`:** Gson comes transitively with `retrofit2-converter-gson`; no extra entry needed in `libs.versions.toml`
- **`kotlinx-coroutines-play-services`:** Needed in `:wear` for `Task.await()` — add if not already present

## Success Metrics

- `StatusScreen` shows values that visibly change as the watch emits simulated readings (no hardcoded numbers)
- Backend server logs show one POST per sensor tick from the phone (not the watch)
- App does not crash when the watch is disconnected or the backend is unreachable

## Open Questions

- What is the final `BASE_URL` for the Node+Postgres backend? A `TODO` placeholder is used until confirmed.
- Should the phone buffer missed readings (e.g., when backend is down) and flush on reconnect? Deferred to a future story.
- Should `stressIndex` be computed on the watch from raw HRV, or sent as a raw HRV delta and computed on the backend? Currently treated as an opaque `Int` from the sensor/simulation.
