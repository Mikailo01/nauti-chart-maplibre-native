package com.bytecause.nautichart.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VesselInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllVessels(vessels: List<VesselInfoEntity>)

    @Query("SELECT * FROM vessel_info")
    fun loadAllVessels(): Flow<List<VesselInfoEntity>>

    @Query("DELETE FROM vessel_info")
    suspend fun deleteAllVessels()

    @Query("SELECT (SELECT COUNT(*) FROM vessel_info) == 0")
    fun isVesselDatabaseEmpty(): Flow<Boolean>

    @Query("SELECT (:currentTimeMillis - (SELECT MIN(timeStamp) FROM vessel_info)) > 600000")
    fun shouldUpdateVesselDatabase(currentTimeMillis: Long): Flow<Boolean>

    @Query("SELECT * FROM vessel_info WHERE id = :id")
    fun searchVesselById(id: Int): Flow<VesselInfoEntity>
}
