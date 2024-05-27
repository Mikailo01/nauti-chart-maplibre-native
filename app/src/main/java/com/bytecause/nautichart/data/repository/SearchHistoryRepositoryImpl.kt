package com.bytecause.nautichart.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.nautichart.data.local.datastore.proto.SearchHistoryRepository
import com.bytecause.nautichart.data.local.datastore.proto.serializers.RecentlySearchedPlaceSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject


private val Context.recentlySearchedPlaceDatastore: DataStore<RecentlySearchedPlaceList> by dataStore(
    fileName = "recently_searched_place_datastore",
    serializer = RecentlySearchedPlaceSerializer
)

class SearchHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SearchHistoryRepository {

    override suspend fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace) {
        withContext(Dispatchers.IO) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().addPlace(entity).build()
            }
        }
    }

    override suspend fun deleteRecentlySearchedPlace(index: Int) {
        withContext(Dispatchers.IO) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().removePlace(index).build()
            }
        }
    }

    override suspend fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>) {
        withContext(Dispatchers.IO) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().clear().build().toBuilder().addAllPlace(entityList).build()
            }
        }
    }

    override fun getRecentlySearchedPlaces(): Flow<RecentlySearchedPlaceList> =
        context.recentlySearchedPlaceDatastore.data
            .flowOn(Dispatchers.IO)
}