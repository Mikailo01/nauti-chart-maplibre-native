package com.bytecause.pois.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.pois.data.remote.RegionDataExtractRemoteDataSource
import com.bytecause.pois.data.repository.abstractions.CountryDataExtractSizeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountryDataExtractSizeRepositoryImpl @Inject constructor(
    private val regionDataExtractRemoteDataSource: RegionDataExtractRemoteDataSource,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountryDataExtractSizeRepository {

    override suspend fun fetchSize(
        continentName: String,
        country: String?
    ): Map<String, String> =
        withContext(coroutineDispatcher) {
            regionDataExtractRemoteDataSource.fetchSize(continentName, country)
        }
}