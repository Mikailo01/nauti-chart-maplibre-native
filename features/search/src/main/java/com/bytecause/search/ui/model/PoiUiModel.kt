package com.bytecause.search.ui.model

data class PoiUiModel(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>
)