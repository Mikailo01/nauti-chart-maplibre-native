package com.bytecause.search.data.repository

import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.SearchedPlaceModel
import com.bytecause.search.data.remote.retrofit.NominatimRestApiService
import com.bytecause.search.data.repository.abstractions.SearchMapRepository
import com.bytecause.search.mapper.asSearchedPlace
import com.bytecause.util.mappers.mapNullInputList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SearchMapRepositoryImpl @Inject constructor(
    private val nominatimRestApiService: NominatimRestApiService,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchMapRepository {

    override suspend fun searchPlaces(query: String): Flow<ApiResult<List<SearchedPlaceModel>>> =
        flow {
            try {
                val response = nominatimRestApiService.search(query)
                if (response.isSuccessful) {
                    emit(
                        ApiResult.Success(
                            mapNullInputList(response.body()) { it.asSearchedPlace() }
                        )
                    )
                } else emit(ApiResult.Failure(exception = Exception("${response.code()}")))
            } catch (e: Exception) {
                emit(ApiResult.Failure(exception = e))
            }
        }
            .flowOn(coroutineDispatcher)
}