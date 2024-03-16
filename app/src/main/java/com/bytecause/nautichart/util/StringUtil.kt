package com.bytecause.nautichart.util

import org.osmdroid.util.GeoPoint
import kotlin.math.round


object StringUtil {

    val searchTypesStringList =
        listOf("shop", "amenity", "leisure", "tourism", "seamark:type", "public_transport")

    fun formatTagString(s: String?): String? {
        s ?: return null
        if (s.contains(";")) return s.replace("_", " ").replace(";", ", ")
            .replaceFirstChar { it.uppercase() }
        return s.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    fun extractCoordinatesToGeoPointList(polygonKml: String): List<GeoPoint> {
        val coordinatesRegex = Regex("<coordinates>(.*?)</coordinates>")
        val matchResult = coordinatesRegex.find(polygonKml)

        val geoPointsList = mutableListOf<GeoPoint>()

        matchResult?.let {
            val coordinatesString = it.groupValues[1]
            val pairs = coordinatesString.split(" ")

            for (pair in pairs) {
                val values = pair.split(",").map { value -> value.toDouble() }
                val geoPoint = GeoPoint(values[1], values[0])
                geoPointsList.add(geoPoint)
            }
        }
        return geoPointsList
    }

    fun formatNumberWithSpaces(number: Int): String {
        val numberString = number.toString()
        val formattedNumber = StringBuilder()

        var count = 0
        for (i in numberString.length - 1 downTo 0) {
            formattedNumber.append(numberString[i])
            count++
            if (count % 3 == 0 && i != 0) {
                formattedNumber.append(' ')
            }
        }
        return formattedNumber.reverse().toString()
    }

    fun formatDistanceDoubleToString(meters: Double?): String {
        meters ?: return ""
        return when {
            meters < 1000 -> round(meters).toInt().toString() + " m"
            meters >= 1000 && ("%.1f".format(meters / 1000)).endsWith(".0") -> ("%.0f".format(
                meters / 1000
            )) + " km"

            else -> ("%.1f".format(meters / 1000)) + " km"
        }
    }

    fun formatBearingDegrees(bearing: Double): String {
        return round(bearing).toInt().toString() + "°"
    }
}