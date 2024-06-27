package com.bytecause.pois.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.ContinentDao
import com.bytecause.data.local.room.dao.CountryDao
import com.bytecause.data.mappers.asContinentCountriesModel
import com.bytecause.data.mappers.asContinentModelList
import com.bytecause.domain.model.ContinentCountriesModel
import com.bytecause.domain.model.ContinentModel
import com.bytecause.pois.data.repository.abstractions.ContinentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContinentRepositoryImpl @Inject constructor(
    private val continentDao: ContinentDao,
    private val countryDao: CountryDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
): ContinentRepository {

    override fun getAllContinents(): Flow<List<ContinentModel>> = continentDao.getAllContinents()
        .map { it.asContinentModelList() }
        .flowOn(coroutineDispatcher)
        .catch {
            emit(listOf())
        }

    override fun getAssociatedCountries(continentId: Int): Flow<ContinentCountriesModel> =
        countryDao.getContinentCountries(continentId)
            .map { it.asContinentCountriesModel() }
            .flowOn(coroutineDispatcher)

}