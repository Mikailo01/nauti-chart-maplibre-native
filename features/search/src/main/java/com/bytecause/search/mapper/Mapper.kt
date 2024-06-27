package com.bytecause.search.mapper

import com.bytecause.domain.model.NominatimApiModel
import com.bytecause.domain.model.SearchedPlace

internal fun List<NominatimApiModel>.asSearchedPlaceList(): List<SearchedPlace> = this.map {
    SearchedPlace(
        placeId = it.placeId,
        latitude = it.lat,
        longitude = it.lon,
        addressType = it.addressType,
        name = it.name,
        displayName = it.displayName,
        polygonCoordinates = it.polygonCoordinates
    )
}