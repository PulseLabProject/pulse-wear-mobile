package com.pl.pulselab.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ApiRepository handles all communication with your backend.
// The ViewModel uses this class; this class uses Retrofit to make HTTP calls.
// Keeping network logic here (not in the ViewModel) means you can swap backends
// or add caching later without touching any UI code.
class ApiRepository {

    // ← REQUIRED: change this to your backend's base URL.
    // Must end with a slash. The path in @POST("readings") is appended to this.
    // Example: "https://api.myserver.com/v1/"
    private val BASE_URL = "https://your-backend.com/api/"

    // OkHttp is the underlying HTTP client (handles sockets, retries, etc.)
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // This interceptor runs on every request — a good place for auth headers.
            // ← Uncomment and fill in if your backend requires authentication:
            // .addHeader("Authorization", "Bearer YOUR_TOKEN_HERE")
            // .addHeader("X-Api-Key", "YOUR_API_KEY")
            val request = chain.request().newBuilder().build()
            chain.proceed(request)
        }
        .build()

    // Retrofit turns ApiService interface methods into real HTTP calls,
    // and automatically converts JSON ↔ Kotlin data classes using Gson.
    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    // Sends one heart rate reading. Returns true if the server accepted it.
    // This is a suspend function — it can be called from a coroutine without
    // blocking the UI thread while waiting for the network response.
    suspend fun sendHeartRate(bpm: Double, timestamp: Long, sessionId: String): Boolean {
        return try {
            val payload = HeartRatePayload(bpm = bpm, timestamp = timestamp, sessionId = sessionId)
            val response = api.postHeartRate(payload)
            response.success
        } catch (e: Exception) {
            // Network call failed: no internet, server unreachable, bad JSON, etc.
            // For MVP this is silent; add logging or UI error state here when ready.
            false
        }
    }
}
