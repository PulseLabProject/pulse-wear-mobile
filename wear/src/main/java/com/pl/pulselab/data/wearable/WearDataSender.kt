package com.pl.pulselab.data.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.pl.pulselab.model.MessagePaths
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.tasks.await

class WearDataSender {
    private val gson = Gson()

    suspend fun send(context: Context, bundle: SensorBundle) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            val payload = gson.toJson(bundle).toByteArray()
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, MessagePaths.SENSOR_DATA, payload)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("WearDataSender", "Failed to send sensor bundle", e)
        }
    }
}
