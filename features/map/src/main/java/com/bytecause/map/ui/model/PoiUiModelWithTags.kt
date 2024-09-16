package com.bytecause.map.ui.model

data class PoiUiModelWithTags(
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String>
)