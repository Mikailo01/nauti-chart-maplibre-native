package com.bytecause.custom_poi.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bytecause.custom_poi.data.local.datastore.proto.serializer.RecentUsedPoiIconSerializer
import com.bytecause.custom_poi.data.repository.abstraction.RecentlyUsedIconsRepository
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private val Context.recentUsedPoiMarkerIconDataStore: DataStore<RecentlyUsedPoiMarkerIconList> by dataStore(
    fileName = "recent_used_poi_icon_datastore",
    serializer = RecentUsedPoiIconSerializer
)

class RecentlyUsedIconsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecentlyUsedIconsRepository {

    override suspend fun addRecentUsedPoiMarkerIconList(iconList: List<RecentlyUsedPoiMarkerIcon>) {
        withContext(coroutineDispatcher) {
            context.recentUsedPoiMarkerIconDataStore.updateData { addIcon: RecentlyUsedPoiMarkerIconList ->
                addIcon.toBuilder().addAllIconDrawableResourceName(iconList).build()
            }
        }
    }

    override suspend fun updateRecentUsedPoiMarkerIconList(newList: List<RecentlyUsedPoiMarkerIcon>) {
        withContext(coroutineDispatcher) {
            context.recentUsedPoiMarkerIconDataStore.updateData {
                it.toBuilder().clear().build().toBuilder().addAllIconDrawableResourceName(newList)
                    .build()
            }
        }
    }

    override fun getRecentUsedPoiMarkerIcons(): Flow<RecentlyUsedPoiMarkerIconList> =
        context.recentUsedPoiMarkerIconDataStore.data
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                if (exception is IOException) emit(
                    RecentlyUsedPoiMarkerIconList.newBuilder().build()
                )
                else throw exception
            }
}