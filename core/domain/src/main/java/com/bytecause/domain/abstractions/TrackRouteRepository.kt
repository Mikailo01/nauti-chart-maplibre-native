package com.bytecause.domain.abstractions

import com.bytecause.domain.model.RouteRecordModel
import kotlinx.coroutines.flow.Flow

interface TrackRouteRepository {
    suspend fun saveRecord(record: RouteRecordModel)
    suspend fun removeRecord(id: Long)
    fun getRecordById(id: Long): Flow<RouteRecordModel?>
    fun getRecordByTimestamp(timestamp: Long): Flow<RouteRecordModel?>
    fun getRecords(): Flow<List<RouteRecordModel>>
}