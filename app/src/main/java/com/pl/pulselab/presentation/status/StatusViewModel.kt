package com.pl.pulselab.presentation.status

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.pulselab.data.network.ApiRepository
import com.pl.pulselab.data.wearable.SensorDataStore
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

private const val TAG = "StatusViewModel"

data class StatusUiState(
    val bundle: SensorBundle? = null,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null,
    val syncError: String? = null,
    val serverOnline: Boolean? = null  // null = not checked yet
)

class StatusViewModel(application: Application) : AndroidViewModel(application) {

    private val apiRepository = ApiRepository()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _statusUiState = MutableStateFlow(StatusUiState())
    val statusUiState: StateFlow<StatusUiState> = _statusUiState.asStateFlow()

    fun injectDebugReading() {
        val bundle = SensorBundle(
            bpm = Random.nextInt(60, 110),
            hrv = Random.nextInt(30, 80),
            spo2 = Random.nextInt(95, 100),
            stressIndex = Random.nextInt(0, 100),
            timestamp = System.currentTimeMillis()
        )
        Log.d(TAG, "Injecting debug reading: $bundle")
        SensorDataStore.sensorFlow.value = bundle
    }

    init {
        Log.d(TAG, "StatusViewModel created, starting sensor collection")

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val online = apiRepository.ping()
                Log.d(TAG, "Ping result: ${if (online) "online" else "unreachable"}")
                _statusUiState.value = _statusUiState.value.copy(serverOnline = online)
                delay(15_000L)
            }
        }

        viewModelScope.launch {
            SensorDataStore.sensorFlow.collect { bundle ->
                if (bundle == null) {
                    Log.d(TAG, "SensorDataStore emitted null — waiting for first reading")
                    return@collect
                }
                Log.d(TAG, "Bundle received: BPM=${bundle.bpm} HRV=${bundle.hrv} SpO2=${bundle.spo2} Stress=${bundle.stressIndex}")
                _statusUiState.value = _statusUiState.value.copy(
                    bundle = bundle,
                    isSyncing = true,
                    syncError = null
                )
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "POSTing to backend...")
                    val result = apiRepository.postReading(bundle)
                    result.fold(
                        onSuccess = {
                            Log.d(TAG, "POST success")
                            _statusUiState.value = _statusUiState.value.copy(
                                isSyncing = false,
                                lastSyncTime = LocalTime.now().format(timeFormatter),
                                syncError = null
                            )
                        },
                        onFailure = { e ->
                            Log.w(TAG, "POST failed: ${e.message}")
                            _statusUiState.value = _statusUiState.value.copy(
                                isSyncing = false,
                                syncError = e.message ?: "Erro de conexão"
                            )
                        }
                    )
                }
            }
        }
    }
}
