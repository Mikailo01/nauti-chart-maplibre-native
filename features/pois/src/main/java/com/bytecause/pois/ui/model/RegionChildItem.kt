package com.bytecause.pois.ui.model

import com.bytecause.domain.model.RegionModel

data class RegionChildItem(
    val regionEntity: RegionModel,
    val isChecked: Boolean,
    val isDownloading: Boolean,
    val isCheckBoxEnabled: Boolean,
    val isDownloaded: Boolean,
    val size: String
)
