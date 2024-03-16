package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.ContinentDao
import com.bytecause.nautichart.data.local.room.CountryDao
import com.bytecause.nautichart.data.local.room.tables.Continent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ContinentDatabaseRepository @Inject constructor(
    continentDao: ContinentDao,
    private val countryDao: CountryDao
) {

    val getAllContinents: Flow<List<Continent>> = continentDao.getAllContinents()
        .flowOn(Dispatchers.IO)
        .catch {
            emit(listOf())
        }
    fun getAssociatedCountries(continentId: Int) = countryDao.getContinentCountries(continentId)
        .flowOn(Dispatchers.IO)

}