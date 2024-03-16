package com.bytecause.nautichart.data.remote.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassRestApiService {

    @GET("interpreter")
    suspend fun makeQuery(
        @Query("data") query: String
    ): Response<ResponseBody>
}