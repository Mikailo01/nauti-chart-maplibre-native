package com.bytecause.nautichart.data.local.datastore.proto

import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepositoryInterface {

    suspend fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace)

    suspend fun deleteRecentlySearchedPlace(index: Int)

    suspend fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>)

    fun getRecentlySearchedPlaces(): Flow<RecentlySearchedPlaceList?>
}