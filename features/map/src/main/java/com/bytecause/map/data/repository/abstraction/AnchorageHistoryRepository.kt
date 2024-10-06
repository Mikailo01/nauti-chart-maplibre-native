package com.bytecause.map.data.repository.abstraction

import com.bytecause.nautichart.AnchorageHistory
import com.bytecause.nautichart.AnchorageHistoryList
import kotlinx.coroutines.flow.Flow

interface AnchorageHistoryRepository {
    suspend fun saveAnchorageHistory(anchorage: AnchorageHistory)
    fun getAnchorageHistoryList(): Flow<AnchorageHistoryList>
    fun getAnchorageHistoryById(id: String): Flow<AnchorageHistory?>
}