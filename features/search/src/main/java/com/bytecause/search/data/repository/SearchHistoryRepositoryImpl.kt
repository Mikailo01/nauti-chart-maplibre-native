package com.bytecause.search.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.datastore.proto.serializer.RecentlySearchedPlaceSerializer
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.recentlySearchedPlaceDatastore: DataStore<RecentlySearchedPlaceList> by dataStore(
    fileName = "recently_searched_place_datastore",
    serializer = RecentlySearchedPlaceSerializer
)

private const val RECENTLY_SEARCHED_PLACE_MAX_SIZE = 50

class SearchHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchHistoryRepository {

    override suspend fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace) {
        withContext(coroutineDispatcher) {
            getRecentlySearchedPlaces().firstOrNull()?.let { searchedPlaces ->

                // Checks if the item exists in the list, if so, removes it and adds it to the end of the list
                searchedPlaces.placeList.find { it.placeId == entity.placeId }?.let {
                    val updatedList = (searchedPlaces.placeList - it) + entity
                    updateRecentlySearchedPlaces(updatedList)
                } ?: run {
                    // when size is at least equal to maximum size, remove the oldest searched element
                    // and add the new one
                    if (searchedPlaces.placeList.size >= RECENTLY_SEARCHED_PLACE_MAX_SIZE) {

                        val updatedList = searchedPlaces.placeList.subList(
                            1,
                            searchedPlaces.placeList.size
                        ) + entity

                        updateRecentlySearchedPlaces(updatedList)
                    } else {
                        context.recentlySearchedPlaceDatastore.updateData {
                            it.toBuilder().addPlace(entity).build()
                        }
                    }
                }
            }
        }
    }

    override suspend fun deleteRecentlySearchedPlace(index: Int) {
        withContext(coroutineDispatcher) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().removePlace(index).build()
            }
        }
    }

    override suspend fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>) {
        withContext(coroutineDispatcher) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().clear().build().toBuilder().addAllPlace(entityList).build()
            }
        }
    }

    override suspend fun clearRecentlySearchedPlaces() {
        withContext(coroutineDispatcher) {
            context.recentlySearchedPlaceDatastore.updateData {
                it.toBuilder().clear().build()
            }
        }
    }

    override fun getRecentlySearchedPlaces(): Flow<RecentlySearchedPlaceList> =
        context.recentlySearchedPlaceDatastore.data
            .flowOn(coroutineDispatcher)
}