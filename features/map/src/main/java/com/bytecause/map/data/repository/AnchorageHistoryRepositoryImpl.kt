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
            applicationContext.anchorageHistoryDataStore.updateData { datastore ->
                datastore.toBuilder()
                    .addAnchorageHistory(anchorage)
                    .build()
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