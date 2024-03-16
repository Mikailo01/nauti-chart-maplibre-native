package com.bytecause.nautichart.domain.model

data class CountryParentItem(val regionList: List<RegionChildItem>, val size: String, val isLoading: Boolean)