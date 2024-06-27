package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CountryRegionsModel
import com.bytecause.domain.model.RegionModel
import kotlinx.coroutines.flow.Flow

interface RegionRepository {
    suspend fun cacheRegions(regionModels: List<RegionModel>)
    fun getRegions(countryId: Int): Flow<CountryRegionsModel>
}