package com.bytecause.presentation.model

import org.maplibre.android.geometry.LatLng

sealed interface PointType {
    data class Poi(val latLng: LatLng, val id: Long) : PointType
    data class Marker(val latLng: LatLng) : PointType
}