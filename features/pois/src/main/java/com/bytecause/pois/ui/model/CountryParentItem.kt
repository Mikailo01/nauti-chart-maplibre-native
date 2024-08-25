package com.bytecause.pois.ui.model

data class CountryParentItem(
    val regionList: List<RegionChildItem>,
    val size: String,
    val isLoading: Boolean = false
)