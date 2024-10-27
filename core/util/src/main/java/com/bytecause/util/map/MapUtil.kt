package com.bytecause.util.map

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
}
