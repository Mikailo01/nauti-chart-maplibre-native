package com.bytecause.map.util

import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.text.isDigitsOnly
import com.bytecause.domain.model.DMS
import com.bytecause.util.color.toHexString
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import java.util.Locale
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sin

object MapUtil {

    fun LatLng.bearingTo(other: LatLng): Double {
        val deltaLon = Math.toRadians(other.longitude - longitude)

        val a1 = Math.toRadians(latitude)
        val b1 = Math.toRadians(other.latitude)

        val y = sin(deltaLon) * cos(b1)
        val x = cos(a1) * sin(b1) - sin(a1) * cos(b1) * cos(deltaLon)
        val result = Math.toDegrees(atan2(y, x))
        return (result + 360.0) % 360.0
    }

    fun LatLng.newLatLngFromDistance(distance: Double, bearing: Double): LatLng {
        val r = 6371000.0 // Earth radius in meters

        // Convert degrees to radians
        val lat1Rad = Math.toRadians(latitude)
        val lon1Rad = Math.toRadians(longitude)
        val bearingRad = Math.toRadians(bearing)

        // Calculate new latitude
        val lat2Rad = asin(
            sin(lat1Rad) * cos(distance / r) + cos(lat1Rad) * sin(distance / r) * cos(bearingRad)
        )

        // Calculate new longitude
        val lon2Rad = lon1Rad + atan2(
            sin(bearingRad) * sin(distance / r) * cos(lat1Rad),
            cos(distance / r) - sin(lat1Rad) * sin(lat2Rad)
        )

        // Convert radians back to degrees
        val lat2 = Math.toDegrees(lat2Rad)
        val lon2 = Math.toDegrees(lon2Rad)

        return LatLng(lat2, lon2)
    }

    fun drawLine(
        polylineList: List<LatLng>,
        lineManager: LineManager?,
        @ColorInt lineColor: Int,
        lineWidth: Float = 2f,
    ) {
        val polyline =
            lineManager?.create(
                LineOptions()
                    .withLatLngs(polylineList)
                    .withLineColor(lineColor.toHexString())
                    .withLineWidth(lineWidth),
            )

        lineManager?.update(polyline)
    }

    fun determineVesselColorType(type: String): Int {
        return when (type) {
            "1" -> com.bytecause.core.resources.R.color.light_pink
            "3" -> com.bytecause.core.resources.R.color.light_blue
            "4" -> com.bytecause.core.resources.R.color.yellow
            "6" -> com.bytecause.core.resources.R.color.dark_blue
            "7" -> com.bytecause.core.resources.R.color.light_green
            "8" -> com.bytecause.core.resources.R.color.light_red
            "9" -> com.bytecause.core.resources.R.color.purple
            else -> com.bytecause.core.resources.R.color.light_gray
        }
    }

    fun determineVesselType(type: String): Int {
        return when (type) {
            "1" -> com.bytecause.core.resources.R.string.vessel_type_1
            "3" -> com.bytecause.core.resources.R.string.vessel_type_3
            "4" -> com.bytecause.core.resources.R.string.vessel_type_4
            "6" -> com.bytecause.core.resources.R.string.vessel_type_6
            "7" -> com.bytecause.core.resources.R.string.vessel_type_7
            "8" -> com.bytecause.core.resources.R.string.vessel_type_8
            "9" -> com.bytecause.core.resources.R.string.vessel_type_9
            else -> com.bytecause.core.resources.R.string.vessel_type_unknown
        }
    }

    fun arePointsWithinDelta(
        point1: LatLng?,
        point2: LatLng?,
        deltaInMeters: Double = 1.0
    ): Boolean {
        if (point1 == null || point2 == null) return false

        val differenceInMeters = point1.distanceTo(point2)

        return differenceInMeters <= deltaInMeters
    }

    /**
     * Calculates the optimal zoom level for a bounding box defined by the
     * specified North-East and South-West geographical coordinates.
     *
     * @param northEast The North-East corner of the bounding box as a [LatLng].
     * @param southWest The South-West corner of the bounding box as a [LatLng].
     * @return The calculated zoom level as a [Double]. The value is constrained
     *         to be at least 1.0, where 1.0 represents the minimum zoom level
     *         for the map.
     */
    fun calculateZoomForBounds(northEast: LatLng, southWest: LatLng): Double {
        var zoomLevel: Double
        val latDiff: Double = northEast.latitude - southWest.latitude
        val lngDiff: Double = northEast.longitude - southWest.longitude

        val maxDiff = max(lngDiff, latDiff)
        zoomLevel = (-1 * ((ln(maxDiff) / ln(2.0)) - (ln(360.0) / ln(2.0))))
        if (zoomLevel < 1) zoomLevel = 1.0

        return round(zoomLevel) // round result to the closest integer for consistency
    }

    fun calculateBoundsForRadius(
        radiusInMeters: Float,
        centerLatitude: Double,
        centerLongitude: Double
    ): LatLngBounds {
        val earthEquatorialRadius = 6378.137
        val m = (1 / ((2 * PI / 360) * earthEquatorialRadius)) / 1000 // 1 meter in degree

        val positiveLatitude = centerLatitude + (radiusInMeters * m)
        val positiveLongitude = centerLongitude + (radiusInMeters * m) / cos(
            centerLatitude * (PI / 180)
        )

        val negativeLatitude = centerLatitude - (radiusInMeters * m)
        val negativeLongitude = centerLongitude - (radiusInMeters * m) / cos(
            centerLatitude * (PI / 180)
        )

        val northEast = LatLng(latitude = positiveLatitude, longitude = positiveLongitude)
        val southWest = LatLng(latitude = negativeLatitude, longitude = negativeLongitude)

        return LatLngBounds.fromLatLngs(listOf(northEast, southWest))
    }

    /** This method take international date line into consideration. **/
    fun isPositionInBoundingBox(
        latLng: LatLng,
        boundingBox: LatLngBounds,
    ): Boolean {
        return if (boundingBox.longitudeEast < boundingBox.longitudeWest) {
            // Bounding box crosses the International Date Line
            (latLng.longitude >= boundingBox.longitudeWest || latLng.longitude <= boundingBox.longitudeEast) &&
                    (latLng.latitude in boundingBox.latitudeSouth..boundingBox.latitudeNorth)
        } else {
            // Bounding box does not cross the International Date Line
            (latLng.longitude in boundingBox.longitudeWest..boundingBox.longitudeEast) &&
                    (latLng.latitude in boundingBox.latitudeSouth..boundingBox.latitudeNorth)
        }
    }

    fun radiusInMetersToRadiusInPixels(
        mapLibreMap: MapLibreMap,
        radiusInMeters: Float,
        latitude: Double
    ): Float {
        val metersPerPixel =
            mapLibreMap.projection.getMetersPerPixelAtLatitude(latitude)
        val radiusInPixels = radiusInMeters / metersPerPixel

        return radiusInPixels.toFloat()
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

    fun areCoordinatesValid(latitude: Double, longitude: Double): Boolean {
        return (latitude in -89.9..89.9) && (longitude in -179.9..179.9)
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
            val parts = coordinates.split(Regex("\\s+|,")).filter { it.isNotEmpty() }

            if (parts.size != 2) return null

            val dmsLatitude = splitDMS(parts[0])
            val dmsLongitude = splitDMS(parts[1])

            if (dmsLatitude == null || dmsLongitude == null) return null

            val decimalLatitude =
                dmsToDecimal(
                    dmsLatitude.degrees,
                    dmsLatitude.minutes,
                    dmsLatitude.seconds,
                    dmsLatitude.direction,
                )
            val decimalLongitude =
                dmsToDecimal(
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
            Log.d(com.bytecause.util.extensions.TAG(this), "else")
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
