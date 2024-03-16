package com.bytecause.nautichart.data.local.datastore.preferences

import com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import kotlinx.coroutines.flow.Flow

interface RecentlyUsedIconsRepositoryInterface {

    suspend fun addRecentUsedPoiMarkerIconList(iconList: List<RecentlyUsedPoiMarkerIcon>)

    suspend fun updateRecentUsedPoiMarkerIconList(newList: List<RecentlyUsedPoiMarkerIcon>)

    fun getRecentUsedPoiMarkerIcons(): Flow<RecentlyUsedPoiMarkerIconList>
}