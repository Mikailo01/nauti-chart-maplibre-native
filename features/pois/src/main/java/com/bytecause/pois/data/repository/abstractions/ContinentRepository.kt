package com.bytecause.pois.data.repository.abstractions

import com.bytecause.domain.model.ContinentCountriesModel
import com.bytecause.domain.model.ContinentModel
import kotlinx.coroutines.flow.Flow

interface ContinentRepository {
    fun getAllContinents(): Flow<List<ContinentModel>>
    fun getAssociatedCountries(continentId: Int): Flow<ContinentCountriesModel>
}