package com.bytecause.domain.model

data class CountryRegionsModel(
    val countryModel: CountryModel = CountryModel(),
    val regionModels: List<RegionModel> = emptyList()
)