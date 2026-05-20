package com.pl.pulselab.model

data class HeartRateReading(
    val bpm: Double,
    val hrv: Int,
    val spo2: Int,
    val stressIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)
