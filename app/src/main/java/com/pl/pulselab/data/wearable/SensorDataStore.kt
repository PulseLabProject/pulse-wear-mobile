package com.pl.pulselab.data.wearable

import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.flow.MutableStateFlow

object SensorDataStore {
    val sensorFlow: MutableStateFlow<SensorBundle?> = MutableStateFlow(null)
}
