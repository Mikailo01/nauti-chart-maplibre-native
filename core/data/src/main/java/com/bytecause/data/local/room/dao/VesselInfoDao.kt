package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.VesselInfoEntity
import kotlinx.coroutines.flow.Flow

private const val INTERVAL = 6_000_000_000

@Dao
interface VesselInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllVessels(vessels: List<VesselInfoEntity>)

    @Query("SELECT * FROM vessel_info")
    fun loadAllVessels(): Flow<List<VesselInfoEntity>>

    @Query("SELECT (SELECT COUNT(*) FROM vessel_info) == 0")
    fun isVesselDatabaseEmpty(): Flow<Boolean>

    @Query("SELECT (:currentTimeMillis - (SELECT MIN(timeStamp) FROM vessel_info)) > $INTERVAL")
    fun shouldUpdateVesselDatabase(currentTimeMillis: Long): Flow<Boolean>

    @Query("SELECT * FROM vessel_info WHERE id = :id")
    fun searchVesselById(id: Int): Flow<VesselInfoEntity>
}