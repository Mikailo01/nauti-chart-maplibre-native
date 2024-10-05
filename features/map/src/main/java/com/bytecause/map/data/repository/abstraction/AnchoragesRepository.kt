package com.bytecause.map.data.repository.abstraction

import com.bytecause.data.local.room.tables.AnchoragesEntity
import kotlinx.coroutines.flow.Flow

interface AnchoragesRepository {
    fun getByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<AnchoragesEntity>>
}