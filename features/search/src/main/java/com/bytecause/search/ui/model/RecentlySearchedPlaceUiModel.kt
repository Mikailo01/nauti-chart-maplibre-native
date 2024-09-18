package com.bytecause.search.ui.model

data class RecentlySearchedPlaceUiModel(
    val placeId: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val displayName: String,
    val type: String,
    val timestamp: Long
)