package com.bytecause.util.map

import com.bytecause.domain.model.MetersUnitConvertConstants
import com.bytecause.util.extensions.toFirstDecimal
import org.maplibre.android.geometry.LatLng
import java.math.RoundingMode

object MapUtil {
    private fun decimalToDMS(decimal: Double): String {
        val degrees = decimal.toInt()
        val minutes = ((decimal - degrees) * 60).toInt()
        val seconds =
            ((decimal - degrees - (minutes / 60.0)) * 3600)
                .toBigDecimal()
                .setScale(0, RoundingMode.HALF_UP)
                .toInt()
        return "${if (degrees < 0) degrees * -1 else degrees}°${if (minutes < 0) minutes * -1 else minutes}'${if (seconds < 0) seconds * -1 else seconds}\""
    }

    // Degrees Minutes Seconds
    fun latitudeToDMS(latitude: Double): String {
        val direction = if (latitude >= 0) "N" else "S"
        return decimalToDMS(latitude) + direction
    }

    // Degrees Minutes Seconds
    fun longitudeToDMS(longitude: Double): String {
        val direction = if (longitude >= 0) "E" else "W"
        return decimalToDMS(longitude) + direction
    }

    fun calculateAndSumDistance(points: List<Pair<Double, Double>>): Double {
        var distance = 0.0
        for (x in points.indices) {
            if (x == points.indices.last) {
                return distance.toFirstDecimal { this / MetersUnitConvertConstants.NauticalMiles.value }
            }

            val currentPair = points[x]
            val nextPair = points[x + 1]

            distance += LatLng(latitude = currentPair.first, longitude = currentPair.second)
                .distanceTo(
                    LatLng(latitude = nextPair.first, longitude = nextPair.second)
                )
        }

        return distance.toFirstDecimal { this / MetersUnitConvertConstants.NauticalMiles.value }
    }
}
