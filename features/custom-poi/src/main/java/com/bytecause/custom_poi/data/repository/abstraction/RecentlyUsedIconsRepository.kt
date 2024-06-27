package com.bytecause.custom_poi.data.repository.abstraction

import com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import kotlinx.coroutines.flow.Flow

interface RecentlyUsedIconsRepository {

    suspend fun addRecentUsedPoiMarkerIconList(iconList: List<RecentlyUsedPoiMarkerIcon>)

    suspend fun updateRecentUsedPoiMarkerIconList(newList: List<RecentlyUsedPoiMarkerIcon>)

    fun getRecentUsedPoiMarkerIcons(): Flow<RecentlyUsedPoiMarkerIconList>
}