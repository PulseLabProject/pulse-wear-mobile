package com.pl.pulselab.model

// A single heart rate measurement captured at a specific moment in time.
// This is the core data unit that flows through the entire app:
// Sensor → HeartRateRepository → HomeViewModel → HomeScreen + ApiRepository → Backend
data class HeartRateReading(
    val bpm: Double,                                    // beats per minute
    val timestamp: Long = System.currentTimeMillis()   // milliseconds since epoch (Unix time)
)
