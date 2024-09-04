package com.bytecause.pois.ui.model

import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.RegionModel

data class RegionChildItem(
    val regionEntity: RegionModel,
    val isChecked: Boolean,
    val loading: Loading = Loading(),
    val isCheckBoxEnabled: Boolean,
    val size: String
)
