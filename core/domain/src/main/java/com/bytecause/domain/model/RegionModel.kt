package com.bytecause.domain.model

data class RegionModel(
    val id: Long = 0,
    val names: Map<String, String> = emptyMap(),
    val countryId: Int = 1
)
