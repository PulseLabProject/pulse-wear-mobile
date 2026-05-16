package com.pl.pulselab.data.network

import android.util.Log
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository {

    private val BASE_URL = "https://your-backend.com/api/"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val tokenResult = runBlocking { Clerk.auth.getToken() }
            val requestBuilder = chain.request().newBuilder()
            when (tokenResult) {
                is ClerkResult.Success -> {
                    val jwt = tokenResult.value
                    if (!jwt.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $jwt")
                    }
                }
                is ClerkResult.Failure -> {
                    Log.w("ApiRepository", "Clerk getToken failed: ${tokenResult.throwable?.message}")
                }
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    suspend fun sendHeartRate(bpm: Double, timestamp: Long, sessionId: String): Boolean {
        return try {
            val payload = HeartRatePayload(bpm = bpm, timestamp = timestamp, sessionId = sessionId)
            val response = api.postHeartRate(payload)
            response.success
        } catch (e: Exception) {
            false
        }
    }
}
