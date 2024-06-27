package com.bytecause.util.map

import androidx.core.text.isDigitsOnly
import com.bytecause.domain.model.DMS
import org.maplibre.android.geometry.LatLng
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MapUtil {
    companion object {

        fun LatLng.bearingTo(other: LatLng): Double {
            val deltaLon = Math.toRadians(other.longitude - longitude)

            val a1 = Math.toRadians(latitude)
            val b1 = Math.toRadians(other.latitude)

            val y = sin(deltaLon) * cos(b1)
            val x = cos(a1) * sin(b1) - sin(a1) * cos(b1) * cos(deltaLon)
            val result = Math.toDegrees(atan2(y, x))
            return (result + 360.0) % 360.0
        }

        fun arePointsWithinDelta(
            point1: LatLng?,
            point2: LatLng?,
            delta: Double = 1e-5,
        ): Boolean {
            if (point1 == null || point2 == null) return false

            val latDifference = abs(point1.latitude - point2.latitude)
            val lngDifference = abs(point1.longitude - point2.longitude)

            return latDifference <= delta && lngDifference <= delta
        }

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

        fun areCoordinatesValid(input: String): Boolean {
            val ddPattern = Regex("""^\d+\.\d+° [NS], \d+\.\d+° [EW]$""")
            val dmsPattern =
                """^\d+°\d+'\d+(\.\d+)?"[NS](,)? ?\d+°\d+'\d+(\.\d+)?"[EW]$""".toRegex()
            val decimalPattern = Regex("""^-?\d+(\.\d+)?,? ?\s*-?\d+(\.\d+)?$""")

            val match =
                ddPattern.matchEntire(input) ?: dmsPattern.matchEntire(input)
                    ?: decimalPattern.matchEntire(input)

            return match != null && validateCoordinatesBounds(match.groupValues)
        }

        // Checks if coordinates are in valid bounds
        private fun validateCoordinatesBounds(groups: List<String>): Boolean {
            var isValid: Boolean
            val pattern = Regex("[^\\d]+")

            groups[0].split(" ").let {
                val latitude = it[0].split(pattern).filter { it.isNotBlank() }
                latitude.forEachIndexed { index, s ->
                    if (s.isDigitsOnly()) {
                        isValid =
                            when (index) {
                                // latitude degrees have to be between 0° - 90°
                                0 -> s.toInt() in 0..90
                                // if latitude minutes and seconds are between 0 - 60 and degrees are smaller
                                // than 90 return true or return false if degrees are equal to 90° and
                                // minutes or seconds are not equal to 0.
                                else -> s.toInt() in 0..60 && latitude[0].toInt() < 90 || s.toInt() == 0
                            }
                        if (!isValid) return false
                    }
                }
                val longitude = it[1].split(pattern).filter { it.isNotBlank() }
                longitude.forEachIndexed { index, s ->
                    if (s.isDigitsOnly()) {
                        isValid =
                            when (index) {
                                // longitude degrees have to be between 0° - 180°
                                0 -> s.toInt() in 0..180
                                // if longitude minutes and seconds are between 0 - 60 and degrees are smaller
                                // than 180 return true or return false if degrees are equal to 180° and
                                // minutes or seconds are not equal to 0.
                                else -> s.toInt() in 0..60 && longitude[0].toInt() < 180 || s.toInt() == 0
                            }
                        if (!isValid) return false
                    }
                }
            }
            return true
        }

        fun stringCoordinatesToGeoPoint(coordinates: String): LatLng? {
            // DMS format
            if (coordinates.contains('"')) {
                val mapUtil = MapUtil()
                val parts = coordinates.split(Regex("\\s+|,")).filter { it.isNotEmpty() }

                if (parts.size != 2) return null

                val dmsLatitude = mapUtil.splitDMS(parts[0])
                val dmsLongitude = mapUtil.splitDMS(parts[1])

                if (dmsLatitude == null || dmsLongitude == null) return null

                val decimalLatitude =
                    mapUtil.dmsToDecimal(
                        dmsLatitude.degrees,
                        dmsLatitude.minutes,
                        dmsLatitude.seconds,
                        dmsLatitude.direction,
                    )
                val decimalLongitude =
                    mapUtil.dmsToDecimal(
                        dmsLongitude.degrees,
                        dmsLongitude.minutes,
                        dmsLongitude.seconds,
                        dmsLongitude.direction,
                    )

                // Format the doubles to five decimal places
                val latitude = String.format(Locale.getDefault(), "%.5f", decimalLatitude)
                val longitude = String.format(Locale.getDefault(), "%.5f", decimalLongitude)

                return LatLng(latitude.toDouble(), longitude.toDouble())
            } else if (coordinates.contains(Regex("[ ,]+"))) {
                val pattern = """^[-?0-9. ]+$""".toRegex()
                val filteredInput =
                    coordinates.replace(',', ' ').filter { char ->
                        pattern.matches(char.toString())
                    }
                val list = filteredInput.split(Regex("[ ,]")).filter { it.isNotEmpty() }

                return LatLng(list[0].toDouble(), list[1].toDouble())
            } else {
                return null
            }
        }
    }

    private fun splitDMS(dmsString: String): DMS? {
        val regex = """^(\d+)°(\d+)'(\d+(\.\d+)?)"([NSWE])""".toRegex()
        val matchResult =
            regex.find(dmsString) ?: throw IllegalArgumentException("Invalid DMS format")

        if (matchResult.groups.size == 6) {
            matchResult.groups.let {
                return DMS(
                    it[1]!!.value.toInt(),
                    it[2]!!.value.toInt(),
                    it[3]!!.value.toDouble().toInt(),
                    it[5]!!.value[0],
                )
            }
        }
        return null
    }

    private fun dmsToDecimal(
        degrees: Int,
        minutes: Int,
        seconds: Int,
        direction: Char,
    ): Double {
        val sign =
            if (direction.uppercaseChar() == 'S' || direction.uppercaseChar() == 'W') -1 else 1
        return sign * (degrees + minutes / 60.0 + seconds / 3600.0)
    }
}
