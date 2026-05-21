package com.pl.pulselab.presentation.status

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.pulselab.data.network.ApiRepository
import com.pl.pulselab.data.wearable.SensorDataStore
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class StatusUiState(
    val bundle: SensorBundle? = null,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null,
    val syncError: String? = null
)

class StatusViewModel(application: Application) : AndroidViewModel(application) {

    private val apiRepository = ApiRepository()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _statusUiState = MutableStateFlow(StatusUiState())
    val statusUiState: StateFlow<StatusUiState> = _statusUiState.asStateFlow()

    fun injectDebugReading() {
        SensorDataStore.sensorFlow.value = SensorBundle(
            bpm = Random.nextInt(60, 110),
            hrv = Random.nextInt(30, 80),
            spo2 = Random.nextInt(95, 100),
            stressIndex = Random.nextInt(0, 100),
            timestamp = System.currentTimeMillis()
        )
    }

    init {
        viewModelScope.launch {
            SensorDataStore.sensorFlow.collect { bundle ->
                bundle ?: return@collect
                _statusUiState.value = _statusUiState.value.copy(
                    bundle = bundle,
                    isSyncing = true,
                    syncError = null
                )
                withContext(Dispatchers.IO) {
                    val result = apiRepository.postReading(bundle)
                    result.fold(
                        onSuccess = {
                            _statusUiState.value = _statusUiState.value.copy(
                                isSyncing = false,
                                lastSyncTime = LocalTime.now().format(timeFormatter),
                                syncError = null
                            )
                        },
                        onFailure = { e ->
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
