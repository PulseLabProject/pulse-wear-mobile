package com.pl.pulselab.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.pulselab.data.health.HeartRateRepository
import com.pl.pulselab.data.wearable.WearDataSender
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

sealed class HomeUiState {
    object Idle : HomeUiState()

    data class Monitoring(
        val bpm: Double?,
        val hrv: Int?,
        val spo2: Int?,
        val stressIndex: Int?,
        val sessionId: String,
        val readingsSent: Int,
        val phoneReachable: Boolean? = null
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val heartRateRepo = HeartRateRepository(application)
    private val wearDataSender = WearDataSender()
    // migrated to phone relay — direct ApiRepository calls removed

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null

    fun startSession() {
        val sessionId = UUID.randomUUID().toString()
        _uiState.value = HomeUiState.Monitoring(
            bpm = null,
            hrv = null,
            spo2 = null,
            stressIndex = null,
            sessionId = sessionId,
            readingsSent = 0
        )

        monitoringJob = heartRateRepo.heartRateFlow()
            .onEach { reading ->
                val current = _uiState.value as? HomeUiState.Monitoring ?: return@onEach

                _uiState.value = current.copy(
                    bpm = reading.bpm,
                    hrv = reading.hrv,
                    spo2 = reading.spo2,
                    stressIndex = reading.stressIndex
                )

                viewModelScope.launch {
                    val bundle = SensorBundle(
                        bpm = reading.bpm.toInt(),
                        hrv = reading.hrv,
                        spo2 = reading.spo2,
                        stressIndex = reading.stressIndex,
                        timestamp = reading.timestamp
                    )
                    val result = wearDataSender.send(getApplication(), bundle)
                    val updated = _uiState.value as? HomeUiState.Monitoring ?: return@launch
                    _uiState.value = updated.copy(phoneReachable = result, readingsSent = updated.readingsSent + 1)
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopSession() {
        monitoringJob?.cancel()
        monitoringJob = null
        _uiState.value = HomeUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
    }
}
