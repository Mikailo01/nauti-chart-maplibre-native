package com.bytecause.domain.model

data class VesselModel(
    val id: Int,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val heading: String = "",
)
