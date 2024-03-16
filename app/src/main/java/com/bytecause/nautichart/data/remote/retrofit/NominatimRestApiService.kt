package com.bytecause.nautichart.data.remote.retrofit

import com.bytecause.nautichart.domain.model.NominatimApiModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimRestApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("polygon_kml") polygonKml: Int = 1
    ): Response<List<NominatimApiModel>>
}