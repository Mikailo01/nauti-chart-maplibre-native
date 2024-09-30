package com.bytecause.search.data.remote.retrofit

import com.bytecause.domain.model.NominatimApiModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimRestApiService {

    // TODO("Add interceptor to be able to add Header dynamically")
    @GET("search")
    @Headers("User-Agent: NautiChart/0.2.0-beta1")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("polygon_kml") polygonKml: Int = 1
    ): Response<List<NominatimApiModel>>
}