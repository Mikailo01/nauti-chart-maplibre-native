package com.bytecause.search.mapper

import com.bytecause.domain.model.NominatimApiModel
import com.bytecause.domain.model.SearchedPlace


internal fun NominatimApiModel.asSearchedPlace(): SearchedPlace =
    SearchedPlace(
        placeId = placeId,
        latitude = lat,
        longitude = lon,
        addressType = addressType,
        name = name,
        displayName = displayName,
        polygonCoordinates = polygonCoordinates
    )