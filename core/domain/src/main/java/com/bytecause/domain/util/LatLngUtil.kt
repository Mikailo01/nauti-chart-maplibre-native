package com.bytecause.domain.util

import com.bytecause.domain.model.LatLngModel
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val DEG2RAD = Math.PI / 180.0
private const val RADIUS_EARTH_METERS = 6378137

fun LatLngModel.distanceTo(other: LatLngModel): Double {
    val lat1: Double = DEG2RAD * latitude
    val lat2: Double = DEG2RAD * other.latitude
    val lon1: Double = DEG2RAD * longitude
    val lon2: Double = DEG2RAD * other.longitude
    return RADIUS_EARTH_METERS * 2 * asin(
        min(
            1.0, sqrt(
                sin((lat2 - lat1) / 2)
                    .pow(2.0) + cos(lat1) * cos(lat2) * sin((lon2 - lon1) / 2)
                    .pow(2.0)
            )
        )
    )
}