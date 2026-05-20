package com.pl.pulselab.data.network

import com.pl.pulselab.model.SensorBundle
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository {

    companion object {
        const val BASE_URL = "https://TODO.replace.me/"
        const val API_TOKEN = "TODO"
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_TOKEN")
                .build()
            chain.proceed(request)
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
