package com.bytecause.presentation.model

sealed interface PlaceType {

    data class Poi(
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double
    ) : PlaceType

    data class Address(val placeModel: SearchedPlaceUiModel) : PlaceType
}