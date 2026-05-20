package com.pl.pulselab.data.wearable

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.pl.pulselab.model.MessagePaths
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != MessagePaths.SENSOR_DATA) return
        scope.launch {
            try {
                val bundle = gson.fromJson(String(event.data), SensorBundle::class.java)
                SensorDataStore.sensorFlow.value = bundle
            } catch (e: Exception) {
                Log.e("WearListenerService", "Failed to decode SensorBundle", e)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
