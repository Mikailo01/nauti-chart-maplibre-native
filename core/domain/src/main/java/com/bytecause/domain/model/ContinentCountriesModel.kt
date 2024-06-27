package com.bytecause.domain.model

data class ContinentCountriesModel(
    val continentModel: ContinentModel = ContinentModel(),
    val countries: List<CountryModel> = emptyList()
)
