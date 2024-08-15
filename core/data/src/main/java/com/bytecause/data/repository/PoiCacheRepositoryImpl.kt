package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.PoiCacheDao
import com.bytecause.data.mappers.asPoiCacheEntity
import com.bytecause.data.mappers.asPoiCacheModel
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.util.poi.PoiUtil
import com.bytecause.util.poi.PoiUtil.getResourceName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PoiCacheRepositoryImpl @Inject constructor(
    private val poiCacheDao: PoiCacheDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PoiCacheRepository {

    override fun loadCachedResults(): Flow<List<PoiCacheModel>> = poiCacheDao.loadCache()
        .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }
        .flowOn(coroutineDispatcher)

    override fun isCacheEmpty(): Flow<Boolean> = poiCacheDao.isCacheEmpty()
        .flowOn(coroutineDispatcher)

    override fun getAllDistinctCategories(): Flow<List<String>> =
        poiCacheDao.getAllDistinctCategories()
            .flowOn(coroutineDispatcher)

    override fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>> =
        poiCacheDao.searchInCache(placeIds)
            .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override fun searchPoiWithInfoById(id: Long): Flow<PoiCacheModel> =
        poiCacheDao.searchPoiWithInfoById(id)
            .map { it.asPoiCacheModel() }
            .flowOn(coroutineDispatcher)

    override fun isPlaceCached(placeId: Long): Flow<Boolean> = poiCacheDao.isPlaceCached(placeId)
        .flowOn(coroutineDispatcher)

    override fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<PoiCacheModel>> =
        poiCacheDao.loadPoiCacheByBoundingBox(minLat, maxLat, minLon, maxLon, selectedCategories)
            .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }


    override suspend fun cacheResult(result: List<PoiCacheModel>) =
        withContext(coroutineDispatcher) {
            result.map {
                it.asPoiCacheEntity(
                    // Extracts drawable resource name from poi's tags
                    getResourceName(PoiUtil.extractCategoryFromPoiEntity(it.tags)
                        .takeIf { category -> !category.isNullOrEmpty() }
                        .let { tagValue -> formatTagString(tagValue) })
                )
            }.let {
                poiCacheDao.cacheResult(it)
            }
        }

    override suspend fun clearCache() {
        withContext(coroutineDispatcher) {
            poiCacheDao.clearCache()
        }
    }
}