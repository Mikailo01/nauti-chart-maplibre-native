package com.bytecause.domain.model

data class HarboursModel(
    val id: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = emptyMap()
)