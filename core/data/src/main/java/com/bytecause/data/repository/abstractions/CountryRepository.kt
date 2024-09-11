package com.bytecause.data.repository.abstractions

import com.bytecause.domain.model.CountryModel
import kotlinx.coroutines.flow.Flow

interface CountryRepository {
    fun getCountryByIso(isoCode: String): Flow<CountryModel>
}