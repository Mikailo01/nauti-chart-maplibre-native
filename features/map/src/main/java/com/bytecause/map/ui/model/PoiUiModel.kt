package com.bytecause.map.ui.model

data class PoiUiModel(
    val id: Long = 0,
    val name: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)