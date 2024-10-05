package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.bytecause.data.local.room.tables.AnchoragesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnchoragesDao {
    @Query("SELECT * FROM anchorages WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    fun loadByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<AnchoragesEntity>>
}