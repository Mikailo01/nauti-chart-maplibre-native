package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.remote.retrofit.OverpassRestApiService
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassElement
import com.bytecause.domain.model.OverpassElementTypeAdapterFactory
import com.bytecause.domain.model.OverpassResponse
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.reflect.KClass

class OverpassRepositoryImpl @Inject constructor(
    private val overpassRestApiService: OverpassRestApiService,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
): OverpassRepository {

    // Overpass API returns different JSON scheme for Nodes and Relations, so we have to pass correct
    // type argument.
    override suspend fun <T: OverpassElement> makeQuery(query: String, clazz: KClass<T>): ApiResult<List<T>> {
        return withContext(coroutineDispatcher) {
            try {
                val response = overpassRestApiService.makeQuery(query)

                if (response.isSuccessful) {
                    val gson = GsonBuilder()
                        .registerTypeAdapterFactory(OverpassElementTypeAdapterFactory())
                        .create()

                    val serializedData: OverpassResponse =
                        gson.fromJson(response.body()?.string(), OverpassResponse::class.java)

                    val filteredList = serializedData.elements.filterIsInstance(clazz.java)
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