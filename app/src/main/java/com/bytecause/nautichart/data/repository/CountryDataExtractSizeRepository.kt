package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.remote.RegionDataExtractRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountryDataExtractSizeRepository @Inject constructor(
    private val regionDataExtractRemoteDataSource: RegionDataExtractRemoteDataSource
) {

    suspend fun fetchSize(continentName: String, country: String? = null): Map<String, String> =
        withContext(Dispatchers.IO) {
            regionDataExtractRemoteDataSource.fetchSize(continentName, country)
        }
}