package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.RegionPoiCacheDao
import com.bytecause.data.mappers.asPoiCacheModel
import com.bytecause.data.mappers.asRegionPoiCacheEntity
import com.bytecause.domain.abstractions.RegionPoiCacheRepository
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

class RegionPoiCacheRepositoryImpl @Inject constructor(
    private val regionPoiCacheDao: RegionPoiCacheDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RegionPoiCacheRepository {

    override fun isCacheEmpty(): Flow<Boolean> = regionPoiCacheDao.isCacheEmpty()
        .flowOn(coroutineDispatcher)

    override fun getAllDistinctCategories(): Flow<List<String>> =
        regionPoiCacheDao.getAllDistinctCategories()
            .flowOn(coroutineDispatcher)

    override fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>> =
        regionPoiCacheDao.searchInCache(placeIds)
            .map { entityList -> entityList.map { entity -> entity.asPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override fun searchPoiWithInfoById(id: Long): Flow<PoiCacheModel> =
        regionPoiCacheDao.searchPoiWithInfoById(id)
            .map { it.asPoiCacheModel() }
            .flowOn(coroutineDispatcher)

    override fun isPlaceCached(placeId: Long): Flow<Boolean> =
        regionPoiCacheDao.isPlaceCached(placeId)
            .flowOn(coroutineDispatcher)

    override fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<PoiCacheModel>> =
        regionPoiCacheDao.loadPoiCacheByBoundingBox(
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
                it.asRegionPoiCacheEntity(
                    // Extracts drawable resource name from poi's tags
                    getResourceName(PoiUtil.extractCategoryFromPoiEntity(it.tags)
                        .takeIf { category -> !category.isNullOrEmpty() }
                        .let { tagValue -> formatTagString(tagValue) })
                )
            }.let {
                regionPoiCacheDao.cacheResult(it)
            }
        }
}