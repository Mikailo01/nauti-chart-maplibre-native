package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.RegionDao
import com.bytecause.data.mappers.asCountryRegionsModel
import com.bytecause.data.mappers.asRegionEntity
import com.bytecause.domain.abstractions.RegionRepository
import com.bytecause.domain.model.CountryRegionsModel
import com.bytecause.domain.model.RegionModel
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegionRepositoryImpl @Inject constructor(
    private val regionDao: RegionDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RegionRepository {
    override suspend fun cacheRegions(regionModels: List<RegionModel>) =
        withContext(coroutineDispatcher) {
            regionDao.cacheRegions(mapList(regionModels) { it.asRegionEntity() })
        }

    override fun getRegions(countryId: Int): Flow<CountryRegionsModel> =
        regionDao.getCountryRegions(countryId)
            .map { it.asCountryRegionsModel() }
            .flowOn(coroutineDispatcher)
}