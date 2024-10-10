package com.bytecause.map.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bytecause.map.data.local.datastore.serializer.AnchorageHistorySerializer
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import com.bytecause.nautichart.AnchorageHistory
import com.bytecause.nautichart.AnchorageHistoryList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.anchorageHistoryDataStore: DataStore<AnchorageHistoryList> by dataStore(
    fileName = "anchorage_history_datastore",
    serializer = AnchorageHistorySerializer
)

class AnchorageHistoryRepositoryImpl @Inject constructor(
    private val applicationContext: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnchorageHistoryRepository {

    override suspend fun saveAnchorageHistory(anchorage: AnchorageHistory) {
        withContext(coroutineDispatcher) {
            applicationContext.anchorageHistoryDataStore.data.firstOrNull().let { datastore ->
                // check if item is already cached
                datastore?.anchorageHistoryList?.find {
                    it.latitude == anchorage.latitude
                            && it.longitude == anchorage.longitude
                            && it.radius == anchorage.radius
                } ?: run {
                    // item is not cached, so save it
                    applicationContext.anchorageHistoryDataStore.updateData { datastore ->
                        datastore.toBuilder()
                            .addAnchorageHistory(anchorage)
                            .build()
                    }
                }
            }
        }
    }

    override suspend fun removeAnchorageHistory(id: String) {
        withContext(coroutineDispatcher) {
            applicationContext.anchorageHistoryDataStore.data.firstOrNull()?.let { datastore ->
                datastore.anchorageHistoryList.find { it.id == id }
                    ?.let { anchorageItem ->
                        val index = datastore.anchorageHistoryList.indexOf(anchorageItem)

                        applicationContext.anchorageHistoryDataStore.updateData {
                            it.toBuilder()
                                .removeAnchorageHistory(index)
                                .build()
                        }
                    }
            }
        }
    }

    override suspend fun updateAnchorageHistoryTimestamp(id: String, timestamp: Long) {
        withContext(coroutineDispatcher) {
            applicationContext.anchorageHistoryDataStore.data.firstOrNull()?.let { datastore ->
                datastore.anchorageHistoryList.find { it.id == id }
                    ?.let { anchorageItem ->
                        val index = datastore.anchorageHistoryList.indexOf(anchorageItem)

                        applicationContext.anchorageHistoryDataStore.updateData {
                            it.toBuilder()
                                .removeAnchorageHistory(index)
                                .addAnchorageHistory(
                                    anchorageItem.toBuilder()
                                        .setTimestamp(timestamp)
                                        .build()
                                )
                                .build()
                        }
                    }

            }
        }
    }

    override fun getAnchorageHistoryList(): Flow<AnchorageHistoryList> =
        applicationContext.anchorageHistoryDataStore.data
            .catch { e ->
                e.printStackTrace()
            }
            .flowOn(coroutineDispatcher)

    override fun getAnchorageHistoryById(id: String): Flow<AnchorageHistory?> =
        applicationContext.anchorageHistoryDataStore.data
            .map { datastore ->
                datastore.anchorageHistoryList.find { item ->
                    item.id == id
                }
            }
            .catch { e ->
                e.printStackTrace()
                emit(null)
            }
            .flowOn(coroutineDispatcher)
}