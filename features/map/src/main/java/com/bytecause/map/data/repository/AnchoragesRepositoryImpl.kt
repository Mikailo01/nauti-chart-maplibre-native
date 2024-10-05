package com.bytecause.map.data.repository

import com.bytecause.data.local.room.dao.AnchoragesDao
import com.bytecause.data.local.room.tables.AnchoragesEntity
import com.bytecause.map.data.repository.abstraction.AnchoragesRepository
import kotlinx.coroutines.flow.Flow

class AnchoragesRepositoryImpl(
    private val anchoragesDao: AnchoragesDao
): AnchoragesRepository {
    override fun getByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<AnchoragesEntity>> = anchoragesDao.loadByBoundingBox(minLat, maxLat, minLon, maxLon)
}