package com.bytecause.domain.abstractions

import com.bytecause.domain.model.PoiCacheModel
import kotlinx.coroutines.flow.Flow

interface PoiCacheRepository {
    fun loadResultsByCategory(category: List<String>): Flow<List<PoiCacheModel>>
    fun isCacheEmpty(): Flow<Boolean>
    fun getAllDistinctCategories(): Flow<List<String>>
    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>>
    fun searchPoiWithInfoById(id: Long): Flow<PoiCacheModel>
    fun isPlaceCached(placeId: Long): Flow<Boolean>
    fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<PoiCacheModel>>

    suspend fun cacheResult(result: List<PoiCacheModel>)
    suspend fun clearCache()
}