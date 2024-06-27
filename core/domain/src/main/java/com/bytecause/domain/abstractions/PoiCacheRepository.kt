package com.bytecause.domain.abstractions

import com.bytecause.domain.model.PoiCacheModel
import kotlinx.coroutines.flow.Flow

interface PoiCacheRepository {
    fun loadCachedResults(): Flow<List<PoiCacheModel>>
    fun isCacheEmpty(): Flow<Boolean>
    fun getAllDistinctCategories(): Flow<List<String>>
    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>>
    fun isPlaceCached(placeId: Long): Flow<Boolean>
    fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<PoiCacheModel>>

    suspend fun cacheResult(result: List<PoiCacheModel>)
    suspend fun clearCache()
}