package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.PoiCacheDao
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PoiCacheRepository @Inject constructor(
    private val poiCacheDao: PoiCacheDao
) {

    val loadCachedResults: Flow<List<PoiCacheEntity>> = poiCacheDao.loadCache()
        .flowOn(Dispatchers.IO)
    val isCacheEmpty: Flow<Boolean> = poiCacheDao.isCacheEmpty()
        .flowOn(Dispatchers.IO)
    val getAllDistinctCategories: Flow<List<String>> = poiCacheDao.getAllDistinctCategories()
        .flowOn(Dispatchers.IO)

    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheEntity>> = poiCacheDao.searchInCache(placeIds)
        .flowOn(Dispatchers.IO)
    fun isPlaceCached(placeId: Long): Flow<Boolean> = poiCacheDao.isPlaceCached(placeId)
        .flowOn(Dispatchers.IO)


    suspend fun cacheResult(result: List<PoiCacheEntity>) {
        withContext(Dispatchers.IO) {
            poiCacheDao.cacheResult(result)
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            poiCacheDao.clearCache()
        }
    }
}