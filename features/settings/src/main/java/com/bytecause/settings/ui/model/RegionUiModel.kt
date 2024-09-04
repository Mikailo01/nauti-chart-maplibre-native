package com.bytecause.settings.ui.model

data class RegionUiModel(
    val regionId: Int,
    val names: Map<String, String>,
    val timestamp: Long
)