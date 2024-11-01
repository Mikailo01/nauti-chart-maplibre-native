package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.RouteRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(recordEntity: RouteRecordEntity)

    @Query("DELETE FROM route_record WHERE :id = id")
    suspend fun removeRecord(id: Long)

    @Query("SELECT * FROM route_record WHERE :id = id")
    fun getRecordById(id: Long): Flow<RouteRecordEntity>

    @Query("SELECT * FROM route_record")
    fun getRecords(): Flow<List<RouteRecordEntity>>
}