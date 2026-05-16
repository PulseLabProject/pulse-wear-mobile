# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
./gradlew build

# Build phone app only
./gradlew :app:assembleDebug

# Build Wear OS app only
./gradlew :wear:assembleDebug

# Run unit tests
./gradlew test

# Run a single module's unit tests
./gradlew :app:test
./gradlew :wear:test

# Run instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest
./gradlew :wear:connectedAndroidTest

# Install debug build
./gradlew :app:installDebug
./gradlew :wear:installDebug

# Lint
./gradlew lint
./gradlew :app:lint
./gradlew :wear:lint
```

## Architecture

PulseLab is a dual-module Android project:

- **`:app`** — Phone companion app. Standard Jetpack Compose with Material3. Entry: `app/src/main/java/com/pl/pulselab/MainActivity.kt`. (Currently minimal scaffolding.)
- **`:wear`** — Wear OS app. Uses `androidx.wear.compose.material3`. Entry: `wear/src/main/java/com/pl/pulselab/presentation/MainActivity.kt`.

Both modules share the same `applicationId` (`com.pl.pulselab`) and Kotlin package root (`com.pl.pulselab`). The wear module uses `presentation` as a sub-package by convention.

All dependency versions are centralized in `gradle/libs.versions.toml` (version catalog). Add new dependencies there, not directly in `build.gradle.kts` files.

### Wear module layer structure

```
wear/src/main/java/com/pl/pulselab/
├── model/
│   └── HeartRateReading.kt        ← shared data types (no Android deps)
├── data/
│   ├── health/
│   │   └── HeartRateRepository.kt ← sensor reads (real or simulated)
│   └── network/
│       ├── ApiService.kt          ← Retrofit endpoint definitions
│       └── ApiRepository.kt      ← HTTP calls, base URL, auth headers
└── presentation/
    ├── MainActivity.kt            ← bootstraps the app, sets theme
    ├── theme/Theme.kt
    └── home/
        ├── HomeViewModel.kt       ← state + business logic
        └── HomeScreen.kt         ← UI only (stateful wrapper + stateless content + previews)
```

**Data flow:** Sensor/Simulator → `HeartRateRepository` → `HomeViewModel` → `HomeScreen` (UI display) + `ApiRepository` (HTTP POST to backend)

**Simulation mode:** `HeartRateRepository` has a `SIMULATE` constant at the top. Set `true` (default) to use generated BPM data on any emulator. Set `false` to read from the real watch sensor (requires physical device + `BODY_SENSORS` runtime permission).

**Backend wiring:** Two places to edit in `ApiRepository.kt`: `BASE_URL` and the auth header interceptor. Endpoint path is set in `ApiService.kt` on the `@POST` annotation. Payload shape is `HeartRatePayload`.

**UI pattern:** `HomeScreen` is split into `HomeScreen(viewModel)` (stateful, called by MainActivity) and `HomeScreenContent(uiState, onStart, onStop)` (stateless, used by previews). All new screens should follow this same split.

**State:** `HomeUiState` is a sealed class with three variants — `Idle`, `Monitoring(bpm, sessionId, readingsSent)`, `Error(message)`. The `when` on this class in both ViewModel and UI must be exhaustive.

## Wear OS vs Phone Compose

The two modules use **different Compose artifact families**:

| Concern | Phone (`:app`) | Wear OS (`:wear`) |
|---|---|---|
| Material | `androidx.compose.material3` | `androidx.wear.compose.material3` |
| Foundation | standard Compose | `androidx.wear.compose.foundation` |
| Lazy lists | `LazyColumn` | `TransformingLazyColumn` |
| Scaffold | `Scaffold` | `AppScaffold` + `ScreenScaffold` |
| Previews | `@Preview` | `@WearPreviewDevices`, `@WearPreviewFontScales` |

Never mix wear-compose imports into the phone module or vice versa.

## Key Config

- **minSdk**: 30 (both modules)
- **targetSdk / compileSdk**: 36 (Android 16 / minor API level 1)
- **Kotlin**: 2.2.10 with Compose compiler plugin (`kotlin-compose`)
- **AGP**: 9.1.1
- The wear module requires `useLibrary("wear-sdk")` in its `build.gradle.kts`
- Compose BOM: `2026.02.01` — always use the BOM for Compose version alignment in the phone module
