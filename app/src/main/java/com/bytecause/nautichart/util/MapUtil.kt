package com.bytecause.nautichart.util

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.DMS
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.math.RoundingMode

class MapUtil {

    companion object {

        fun determineVesselColorType(type: String): Int {
            return when (type) {
                "1" -> R.color.light_pink
                "3" -> R.color.light_blue
                "4" -> R.color.yellow
                "6" -> R.color.dark_blue
                "7" -> R.color.light_green
                "8" -> R.color.light_red
                "9" -> R.color.purple
                else -> R.color.light_gray
            }
        }

        fun determineVesselType(type: String): Int {
            return when (type) {
                "1" -> R.string.vessel_type_1
                "3" -> R.string.vessel_type_3
                "4" -> R.string.vessel_type_4
                "6" -> R.string.vessel_type_6
                "7" -> R.string.vessel_type_7
                "8" -> R.string.vessel_type_8
                "9" -> R.string.vessel_type_9
                else -> R.string.vessel_type_unknown
            }
        }

        /** This method take international date line into consideration. **/
        fun isPositionInBoundingBox(geoPoint: GeoPoint, boundingBox: BoundingBox): Boolean {
            return if (boundingBox.lonEast < boundingBox.lonWest) {
                // Bounding box crosses the International Date Line
                (geoPoint.longitude >= boundingBox.lonWest || geoPoint.longitude <= boundingBox.lonEast) &&
                        (geoPoint.latitude in boundingBox.latSouth..boundingBox.latNorth)
            } else {
                // Bounding box does not cross the International Date Line
                (geoPoint.longitude in boundingBox.lonWest..boundingBox.lonEast) &&
                        (geoPoint.latitude in boundingBox.latSouth..boundingBox.latNorth)
            }
        }

        private fun decimalToDMS(decimal: Double): String {
            val degrees = decimal.toInt()
            val minutes = ((decimal - degrees) * 60).toInt()
            val seconds = ((decimal - degrees - (minutes / 60.0)) * 3600)
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
                        isValid = when (index) {
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
                        isValid = when (index) {
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

        fun stringCoordinatesToGeoPoint(coordinates: String): GeoPoint? {
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
                        dmsLatitude.direction
                    )
                val decimalLongitude =
                    mapUtil.dmsToDecimal(
                        dmsLongitude.degrees,
                        dmsLongitude.minutes,
                        dmsLongitude.seconds,
                        dmsLongitude.direction
                    )

                // Format the doubles to five decimal places
                val latitude = String.format("%.5f", decimalLatitude)
                val longitude = String.format("%.5f", decimalLongitude)

                return GeoPoint(latitude.toDouble(), longitude.toDouble())

            } else if (coordinates.contains(Regex("[ ,]+"))) {
                Log.d(TAG(this), "else")
                val pattern = """^[-?0-9. ]+$""".toRegex()
                val filteredInput = coordinates.replace(',', ' ').filter { char ->
                    pattern.matches(char.toString())
                }
                val list = filteredInput.split(Regex("[ ,]")).filter { it.isNotEmpty() }

                return GeoPoint(list[0].toDouble(), list[1].toDouble())
            } else return null
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
                    it[5]!!.value[0]
                )
            }
        }
        return null
    }

    private fun dmsToDecimal(
        degrees: Int,
        minutes: Int,
        seconds: Int,
        direction: Char
    ): Double {
        val sign =
            if (direction.uppercaseChar() == 'S' || direction.uppercaseChar() == 'W') -1 else 1
        return sign * (degrees + minutes / 60.0 + seconds / 3600.0)
    }
}
