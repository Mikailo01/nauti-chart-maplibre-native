package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.RadiusPoiCacheDao
import com.bytecause.data.mappers.asRadiusPoiCacheEntity
import com.bytecause.data.mappers.asRadiusPoiCacheModel
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.model.RadiusPoiCacheModel
import com.bytecause.util.mappers.mapList
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

    override fun searchPoiWithInfoById(id: Long): Flow<RadiusPoiCacheModel?> =
        radiusPoiCacheDao.searchPoiWithInfoById(id)
            .map { it?.asRadiusPoiCacheModel() }
            .flowOn(coroutineDispatcher)

    override fun getPoiByCategory(category: List<String>): Flow<List<RadiusPoiCacheModel>> =
        radiusPoiCacheDao.getPoiByCategory(category)
            .map { originalList -> mapList(originalList) { it.asRadiusPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override fun searchInCache(placeIds: List<Long>): Flow<List<RadiusPoiCacheModel>> =
        radiusPoiCacheDao.searchInCache(placeIds)
            .map { originalList -> mapList(originalList) { it.asRadiusPoiCacheModel() } }
            .flowOn(coroutineDispatcher)

    override suspend fun cacheResult(result: List<RadiusPoiCacheModel>) {
        withContext(coroutineDispatcher) {
            radiusPoiCacheDao.cacheResult(mapList(result) { it.asRadiusPoiCacheEntity() })
        }
    }
}