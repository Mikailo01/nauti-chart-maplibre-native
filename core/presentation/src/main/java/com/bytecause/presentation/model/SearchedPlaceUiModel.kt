package com.bytecause.presentation.model

data class SearchedPlaceUiModel(
    val placeId: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val addressType: String = "",
    val name: String = "",
    val displayName: String = "",
    val polygonCoordinates: String = ""
)