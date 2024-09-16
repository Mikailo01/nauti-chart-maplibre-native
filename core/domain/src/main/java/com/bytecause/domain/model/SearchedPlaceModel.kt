package com.bytecause.domain.model

data class SearchedPlaceModel(
    val placeId: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val addressType: String = "",
    val name: String = "",
    val displayName: String = "",
    val polygonCoordinates: String = ""
)