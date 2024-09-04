package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.RadiusPoiCacheDao
import com.bytecause.data.mappers.asPoiCacheModel
import com.bytecause.data.mappers.asRadiusPoiCacheEntity
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.util.mappers.mapList
import com.bytecause.util.poi.PoiUtil
import com.bytecause.util.poi.PoiUtil.getResourceName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RadiusPoiCacheRepositoryImpl(
    private val radiusPoiCacheDao: RadiusPoiCacheDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RadiusPoiCacheRepository {
    override fun loadByCategory(category: List<String>): Flow<List<PoiCacheModel>> =
        radiusPoiCacheDao.loadByCategory(category)
            .map { entityList -> mapList(entityList) { it.asPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override fun isCacheEmpty(): Flow<Boolean> = radiusPoiCacheDao.isCacheEmpty()
        .flowOn(coroutineDispatcher)

    override fun getAllDistinctCategories(): Flow<List<String>> =
        radiusPoiCacheDao.getAllDistinctCategories()
            .flowOn(coroutineDispatcher)

    override fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>> =
        radiusPoiCacheDao.searchInCache(placeIds)
            .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override fun searchPoiWithInfoById(id: Long): Flow<PoiCacheModel> =
        radiusPoiCacheDao.searchPoiWithInfoById(id)
            .map { it.asPoiCacheModel() }
            .flowOn(coroutineDispatcher)

    override fun isPlaceCached(placeId: Long): Flow<Boolean> =
        radiusPoiCacheDao.isPlaceCached(placeId)
            .flowOn(coroutineDispatcher)

    override fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<PoiCacheModel>> =
        radiusPoiCacheDao.loadPoiCacheByBoundingBox(
            minLat,
            maxLat,
            minLon,
            maxLon,
            selectedCategories
        )
            .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }


    override suspend fun cacheResult(result: List<PoiCacheModel>) =
        withContext(coroutineDispatcher) {
            result.map {
                it.asRadiusPoiCacheEntity(
                    // Extracts drawable resource name from poi's tags
                    getResourceName(
                        PoiUtil.extractCategoryFromPoiEntity(it.tags)
                            .takeIf { category -> !category.isNullOrEmpty() }
                            .let { tagValue -> formatTagString(tagValue) })
                )
            }.let {
                radiusPoiCacheDao.cacheResult(it)
            }
        }

    override suspend fun clearCache() {
        withContext(coroutineDispatcher) {
            radiusPoiCacheDao.clearCache()
        }
    }
}