package com.bytecause.domain.abstractions

import com.bytecause.domain.model.RadiusPoiCacheModel
import kotlinx.coroutines.flow.Flow

interface RadiusPoiCacheRepository {
    fun getPoiByCategory(category: List<String>): Flow<List<RadiusPoiCacheModel>>
    fun searchInCache(placeIds: List<Long>): Flow<List<RadiusPoiCacheModel>>
    fun searchPoiWithInfoById(id: Long): Flow<RadiusPoiCacheModel?>
    suspend fun cacheResult(result: List<RadiusPoiCacheModel>)
}