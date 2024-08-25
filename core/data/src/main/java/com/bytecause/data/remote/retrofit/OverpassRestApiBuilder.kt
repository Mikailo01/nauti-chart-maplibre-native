package com.bytecause.data.remote.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class OverpassRestApiBuilder {

    // Create an OkHttpClient builder
    private val httpClientBuilder = OkHttpClient.Builder().apply {
        readTimeout(240, TimeUnit.SECONDS)
        writeTimeout(240, TimeUnit.SECONDS)
    }

    // Create the OkHttpClient
    private val okHttpClient = httpClientBuilder.build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://overpass-api.de/api/")
        .build()

    fun overpassSearch(): OverpassRestApiService {
        return retrofit.create(OverpassRestApiService::class.java)
    }
}