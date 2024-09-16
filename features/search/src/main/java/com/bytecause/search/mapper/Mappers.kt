package com.bytecause.search.mapper

import com.bytecause.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.domain.model.NominatimApiModel
import com.bytecause.domain.model.SearchedPlaceModel
import com.bytecause.presentation.model.SearchedPlaceUiModel


internal fun NominatimApiModel.asSearchedPlace(): SearchedPlaceModel =
    SearchedPlaceModel(
        placeId = placeId,
        latitude = lat,
        longitude = lon,
        addressType = addressType,
        name = name,
        displayName = displayName,
        polygonCoordinates = polygonCoordinates
    )

internal fun SearchedPlaceModel.asSearchedPlaceUiModel(): SearchedPlaceUiModel =
    SearchedPlaceUiModel(
        placeId = placeId,
        latitude = latitude,
        longitude = longitude,
        addressType = addressType,
        name = name,
        displayName = displayName,
        polygonCoordinates = polygonCoordinates
    )

internal fun SearchPlaceCacheEntity.asSearchedPlaceUiModel(): SearchedPlaceUiModel =
    SearchedPlaceUiModel(
        placeId = placeId.toLong(),
        latitude = latitude,
        longitude = longitude,
        addressType = addressType,
        name = name,
        displayName = displayName,
        polygonCoordinates = polygonCoordinates
    )