package com.bytecause.search.mapper

import com.bytecause.search.data.local.appsearch.SearchPlaceCacheEntity
import com.bytecause.domain.model.NominatimApiModel
import com.bytecause.domain.model.SearchedPlaceModel
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.search.ui.model.RecentlySearchedPlaceUiModel


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

internal fun SearchedPlaceUiModel.asRecentlySearchedPlaceUiModel(): RecentlySearchedPlaceUiModel =
    RecentlySearchedPlaceUiModel(
        placeId = placeId,
        latitude = latitude,
        longitude = longitude,
        name = name,
        displayName = displayName,
        type = addressType,
        timestamp = System.currentTimeMillis()

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

internal fun RecentlySearchedPlace.asRecentlySearchedPlaceUiModel(): RecentlySearchedPlaceUiModel =
    RecentlySearchedPlaceUiModel(
        placeId = placeId,
        latitude = latitude,
        longitude = longitude,
        name = name,
        displayName = displayName,
        type = type,
        timestamp = timeStamp
    )

internal fun RecentlySearchedPlaceUiModel.asRecentlySearchedPlace(): RecentlySearchedPlace =
    RecentlySearchedPlace.newBuilder()
        .setPlaceId(placeId)
        .setLatitude(latitude)
        .setLongitude(longitude)
        .setName(name)
        .setDisplayName(displayName)
        .setType(type)
        .setTimeStamp(timestamp)
        .build()