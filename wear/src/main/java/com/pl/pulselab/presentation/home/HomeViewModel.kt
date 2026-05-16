package com.pl.pulselab.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.pulselab.data.health.HeartRateRepository
import com.pl.pulselab.data.network.ApiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

// ─── UI STATE ───────────────────────────────────────────────────────────────
// Sealed class = the screen can only ever be in one of these three exact states.
// Using a sealed class forces the UI to handle every possible situation.
sealed class HomeUiState {
    // App is open but not yet monitoring
    object Idle : HomeUiState()

    // Actively collecting and sending heart rate data
    data class Monitoring(
        val bpm: Double?,          // null = sensor connected, waiting for first reading
        val sessionId: String,     // unique ID grouping all readings in this session
        val readingsSent: Int      // counter shown in the UI so you can verify backend is receiving
    ) : HomeUiState()

    // Something went wrong (network error, sensor error, etc.)
    data class Error(val message: String) : HomeUiState()
}
// ────────────────────────────────────────────────────────────────────────────

// HomeViewModel is the brain of the home screen.
// It survives screen rotations, holds all screen state, and coordinates
// between the sensor (HeartRateRepository) and the backend (ApiRepository).
// Rule: no Android/Compose imports here — logic only.
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val heartRateRepo = HeartRateRepository(application)
    private val apiRepo = ApiRepository()

    // _uiState is private and mutable — only this ViewModel can change it
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    // uiState is public and read-only — the UI observes this
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Holds the currently running monitoring coroutine so we can cancel it on Stop
    private var monitoringJob: Job? = null

    fun startSession() {
        val sessionId = UUID.randomUUID().toString()
        _uiState.value = HomeUiState.Monitoring(
            bpm = null,
            sessionId = sessionId,
            readingsSent = 0
        )

        // Start collecting from the sensor Flow.
        // launchIn(viewModelScope) means: run until the ViewModel is destroyed or we cancel.
        monitoringJob = heartRateRepo.heartRateFlow()
            .onEach { reading ->
                val current = _uiState.value as? HomeUiState.Monitoring ?: return@onEach

                // 1. Update the displayed BPM immediately (user sees it right away)
                _uiState.value = current.copy(bpm = reading.bpm)

                // 2. Send the reading to the backend concurrently (doesn't block the sensor flow)
                viewModelScope.launch {
                    val sent = apiRepo.sendHeartRate(
                        bpm = reading.bpm,
                        timestamp = reading.timestamp,
                        sessionId = sessionId
                    )
                    if (sent) {
                        // Increment the "sent" counter so you can see data is flowing
                        val updated = _uiState.value as? HomeUiState.Monitoring ?: return@launch
                        _uiState.value = updated.copy(readingsSent = updated.readingsSent + 1)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopSession() {
        monitoringJob?.cancel()  // stops the sensor flow collection
        monitoringJob = null
        _uiState.value = HomeUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel() // safety cleanup when ViewModel is destroyed
    }
}
