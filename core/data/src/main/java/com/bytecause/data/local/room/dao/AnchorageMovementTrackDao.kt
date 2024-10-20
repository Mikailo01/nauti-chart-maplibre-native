package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.AnchorageMovementTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnchorageMovementTrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPosition(position: AnchorageMovementTrackEntity)

    @Query("DELETE FROM anchorage_movement_track")
    suspend fun clear()

    @Query("SELECT * FROM anchorage_movement_track")
    fun getTracks(): Flow<List<AnchorageMovementTrackEntity>>
}