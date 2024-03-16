package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.RegionDao
import com.bytecause.nautichart.data.local.room.tables.Region
import javax.inject.Inject

class RegionRepository @Inject constructor(
    private val regionDao: RegionDao
) {

    suspend fun cacheRegions(regions: List<Region>) = regionDao.cacheRegions(regions)

    fun getRegions(countryId: Int) = regionDao.getCountryRegions(countryId)
}