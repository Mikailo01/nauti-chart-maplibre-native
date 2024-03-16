package com.bytecause.nautichart.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import com.bytecause.nautichart.data.local.datastore.preferences.RecentlyUsedIconsRepositoryInterface
import com.bytecause.nautichart.data.local.datastore.proto.serializers.RecentUsedPoiIconSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
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

class RecentlyUsedIconsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : RecentlyUsedIconsRepositoryInterface {

    override suspend fun addRecentUsedPoiMarkerIconList(iconList: List<RecentlyUsedPoiMarkerIcon>) {
        withContext(Dispatchers.IO) {
            context.recentUsedPoiMarkerIconDataStore.updateData { addIcon: RecentlyUsedPoiMarkerIconList ->
                addIcon.toBuilder().addAllIconDrawableResourceName(iconList).build()
            }
        }
    }

    override suspend fun updateRecentUsedPoiMarkerIconList(newList: List<RecentlyUsedPoiMarkerIcon>) {
        withContext(Dispatchers.IO) {
            context.recentUsedPoiMarkerIconDataStore.updateData {
                it.toBuilder().clear().build().toBuilder().addAllIconDrawableResourceName(newList)
                    .build()
            }
        }
    }

    override fun getRecentUsedPoiMarkerIcons(): Flow<RecentlyUsedPoiMarkerIconList> =
        context.recentUsedPoiMarkerIconDataStore.data
            .flowOn(Dispatchers.IO)
            .catch { exception ->
                if (exception is IOException) emit(
                    RecentlyUsedPoiMarkerIconList.newBuilder().build()
                )
                else throw exception
            }
}