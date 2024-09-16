package com.bytecause.domain.model

data class RadiusPoiCacheModel(
    val placeId: Long = 0,
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = emptyMap(),
    val datasetCategoryName: String = ""
)