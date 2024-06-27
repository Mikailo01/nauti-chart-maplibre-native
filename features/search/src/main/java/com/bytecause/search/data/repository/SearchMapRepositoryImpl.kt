package com.bytecause.search.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.SearchedPlace
import com.bytecause.search.data.remote.retrofit.NominatimRestApiService
import com.bytecause.search.data.repository.abstractions.SearchMapRepository
import com.bytecause.search.mapper.asSearchedPlaceList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SearchMapRepositoryImpl @Inject constructor(
    private val nominatimRestApiService: NominatimRestApiService,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchMapRepository {

    override suspend fun searchPlaces(query: String): Flow<ApiResult<List<SearchedPlace>>> = flow {
        try {
            val response = nominatimRestApiService.search(query)
            if (response.isSuccessful) {
                emit(
                    ApiResult.Success(
                        response.body()?.asSearchedPlaceList()
                            ?: emptyList()
                    )
                )
            } else emit(ApiResult.Failure(exception = Exception("${response.code()}")))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiResult.Failure(exception = e))
        }
    }
        .flowOn(coroutineDispatcher)
}