package com.bytecause.search.data.remote.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NominatimRestApiBuilder {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getSearchApiService(): NominatimRestApiService {
        return retrofit.create(NominatimRestApiService::class.java)
    }
}