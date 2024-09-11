package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.CountryDao
import com.bytecause.data.repository.abstractions.CountryRepository
import com.bytecause.domain.model.CountryModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class CountryRepositoryImpl(
    private val countryDao: CountryDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountryRepository {
    override fun getCountryByIso(isoCode: String): Flow<CountryModel> =
        countryDao.getCountryByIso(isoCode)
            .flowOn(coroutineDispatcher)
}