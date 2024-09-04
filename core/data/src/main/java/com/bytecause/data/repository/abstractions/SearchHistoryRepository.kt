package com.bytecause.data.repository.abstractions

import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace)
    suspend fun deleteRecentlySearchedPlace(index: Int)
    suspend fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>)
    suspend fun clearRecentlySearchedPlaces()
    fun getRecentlySearchedPlaces(): Flow<RecentlySearchedPlaceList>
}