package com.pl.pulselab.data.health

import android.content.Context
import com.pl.pulselab.model.HeartRateReading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ─── SIMULATION SWITCH ──────────────────────────────────────────────────────
// Set to true  → generates fake heart rate data (works on any emulator, no sensor needed)
// Set to false → reads from the real watch heart rate sensor (requires physical Wear OS watch
//                with BODY_SENSORS permission granted at runtime)
private const val SIMULATE = true
// ────────────────────────────────────────────────────────────────────────────

// HeartRateRepository is the single source of truth for heart rate data.
// The ViewModel talks to this class; this class talks to the sensor (or simulator).
// Nothing above this layer knows or cares whether the data is real or simulated.
class HeartRateRepository(private val context: Context) {

    // Returns a Flow — think of it as a pipe that continuously delivers heart rate readings.
    // The caller collects values from this pipe as they arrive.
    fun heartRateFlow(): Flow<HeartRateReading> {
        return if (SIMULATE) simulatedHeartRateFlow() else realHeartRateFlow()
    }

    // Generates realistic-looking heart rate values without any hardware.
    // Useful for: emulator testing, UI development, backend integration testing.
    private fun simulatedHeartRateFlow(): Flow<HeartRateReading> = flow {
        var bpm = 72.0
        while (true) {
            // Drift the BPM slightly each second to mimic a real signal
            bpm = (bpm + (Math.random() - 0.5) * 5).coerceIn(50.0, 130.0)
            emit(HeartRateReading(bpm = bpm))
            delay(1000L) // one reading per second
        }
    }

    // Reads from the watch's physical heart rate sensor via Health Services.
    // TODO: implement this when you're ready to test on a real device.
    //
    // Steps to enable:
    //   1. Set SIMULATE = false above
    //   2. Add BODY_SENSORS to AndroidManifest.xml (already done)
    //   3. Request BODY_SENSORS permission at runtime in HomeScreen before starting a session
    //   4. Replace the placeholder below with the real implementation using:
    //      val measureClient = HealthServices.getClient(context).measureClient
    //      Use callbackFlow { } to bridge the MeasureCallback into a Flow
    private fun realHeartRateFlow(): Flow<HeartRateReading> = flow {
        throw NotImplementedError("Switch SIMULATE to true, or implement real sensor reading here")
    }
}
