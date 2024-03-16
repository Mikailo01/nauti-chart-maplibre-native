package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.remote.retrofit.OverpassRestApiService
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.OverpassElement
import com.bytecause.nautichart.domain.model.OverpassElementTypeAdapterFactory
import com.bytecause.nautichart.domain.model.OverpassResponse
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class OverpassRepository(
    val overpassRestApiService: OverpassRestApiService
) {

    suspend inline fun <reified T : OverpassElement> makeQuery(query: String): ApiResult<List<T>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = overpassRestApiService.makeQuery(query)

                if (response.isSuccessful) {
                    val gson = GsonBuilder()
                        .registerTypeAdapterFactory(OverpassElementTypeAdapterFactory())
                        .create()

                    val serializedData: OverpassResponse =
                        gson.fromJson(response.body()?.string(), OverpassResponse::class.java)

                    val filteredList = serializedData.elements.filterIsInstance<T>()
                    ApiResult.Success(data = filteredList)
                } else {
                    ApiResult.Failure(exception = Exception("${response.code()}"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Failure(exception = e)
            }
        }
    }
}