package com.pl.pulselab.data.network

import android.util.Log
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import com.pl.pulselab.model.SensorBundle
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository {

    companion object {
        const val BASE_URL = "https://TODO.replace.me/"
    }

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

    suspend fun postReading(bundle: SensorBundle): Result<Unit> = runCatching {
        val response = api.postReading(bundle)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()}")
        }
    }
}
