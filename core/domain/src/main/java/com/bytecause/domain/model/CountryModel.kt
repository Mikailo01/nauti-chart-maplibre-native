package com.bytecause.domain.model

data class CountryModel(
    val id: Int = 0,
    val name: String = "",
    val iso2: String = "",
    val iso3: String = "",
    val continentId: Int = 1
)
