package com.bytecause.domain.model

data class RadiusPoiMetadataDatasetModel(
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val timestamp: Long
)