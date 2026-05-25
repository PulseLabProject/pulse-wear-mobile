package com.pl.pulselab.data.network

import com.pl.pulselab.model.SensorBundle
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("v1/readings")
    suspend fun postReading(@Body bundle: SensorBundle): Response<Unit>

    @GET("server/ping")
    suspend fun ping(): Response<Unit>
}
