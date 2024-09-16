package com.bytecause.search.data.repository.abstractions

import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.SearchedPlaceModel
import kotlinx.coroutines.flow.Flow

interface SearchMapRepository {
    suspend fun searchPlaces(query: String): Flow<ApiResult<List<SearchedPlaceModel>>>
}