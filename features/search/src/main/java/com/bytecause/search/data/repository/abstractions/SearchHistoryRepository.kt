package com.bytecause.search.data.repository.abstractions

import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace)

    suspend fun deleteRecentlySearchedPlace(index: Int)

    suspend fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>)

    fun getRecentlySearchedPlaces(): Flow<RecentlySearchedPlaceList>
}