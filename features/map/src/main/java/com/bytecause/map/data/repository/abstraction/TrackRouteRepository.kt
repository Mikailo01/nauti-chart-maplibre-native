package com.bytecause.map.data.repository.abstraction

import com.bytecause.data.local.room.tables.RouteRecordEntity
import kotlinx.coroutines.flow.Flow

interface TrackRouteRepository {
    suspend fun saveRecord(record: RouteRecordEntity)
    suspend fun removeRecord(id: Long)
    fun getRecordById(id: Long): Flow<RouteRecordEntity>
    fun getRecords(): Flow<List<RouteRecordEntity>>
}