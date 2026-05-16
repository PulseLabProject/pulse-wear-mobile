package com.pl.pulselab.data.network

import retrofit2.http.Body
import retrofit2.http.POST

// ApiService describes your backend's HTTP endpoints.
// One function = one API call. Retrofit generates the actual HTTP code for you.
interface ApiService {

    // Sends a heart rate reading to your server.
    // ← Change "readings" to whatever your endpoint path is (e.g. "heart-rate", "data/pulse")
    @POST("readings")
    suspend fun postHeartRate(@Body payload: HeartRatePayload): HeartRateResponse
}

// The JSON body sent in the POST request.
// ← Rename/add fields to match exactly what your backend expects.
data class HeartRatePayload(
    val bpm: Double,        // heart rate value
    val timestamp: Long,   // Unix milliseconds
    val sessionId: String  // groups readings from one monitoring session
)

// The JSON your backend sends back in the response.
// ← Adjust to match your actual API response shape.
data class HeartRateResponse(
    val success: Boolean,
    val message: String? = null
)
