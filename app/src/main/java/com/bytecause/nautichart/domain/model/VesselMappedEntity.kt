package com.bytecause.nautichart.domain.model

data class VesselMappedEntity(
    val id: String,
    val latitude: String = "",
    val longitude: String = "",
    val type: String = "",
    val heading: String = ""
)
