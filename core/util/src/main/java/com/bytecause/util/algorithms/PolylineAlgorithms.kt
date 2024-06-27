package com.bytecause.util.algorithms

import org.maplibre.android.geometry.LatLng
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PolylineAlgorithms {
    /** Encode/decode algorithms from Google Maps API PolyUtil.java **/

    fun decode(encodedPath: String): List<LatLng> {
        val len = encodedPath.length

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        val path: MutableList<LatLng> = ArrayList()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(LatLng(lat * 1e-5, lng * 1e-5))
        }
        return path
    }

    fun encode(path: List<LatLng?>): String {
        var lastLat: Long = 0
        var lastLng: Long = 0
        val result = StringBuffer()
        for (point in path) {
            point?.let { geoPoint ->
                val lat = Math.round(geoPoint.latitude * 1e5)
                val lng = Math.round(geoPoint.longitude * 1e5)
                val dLat = lat - lastLat
                val dLng = lng - lastLng
                encode(dLat, result)
                encode(dLng, result)
                lastLat = lat
                lastLng = lng
            }
        }
        return result.toString()
    }

    private fun encode(
        v: Long,
        result: StringBuffer,
    ) {
        var value = v
        // inv() switches integer sign.
        value = if (value < 0) (value shl 1).inv() else value shl 1
        while (value >= 0x20) {
            result.append(Character.toChars(((0x20L or (value and 0x1fL)) + 63).toInt()))
            value = value shr 5
        }
        result.append(Character.toChars((value + 63).toInt()))
    }

    /** Douglas-Peucker decimation algorithm. Country's boundaries wouldn't fit into database, so
     * we need to lower the count of polyline points. **/
    fun simplifyPolyline(
        points: List<LatLng>,
        epsilon: Double,
    ): List<LatLng> {
        // Find the point with the maximum distance
        var dmax = 0.0
        var index = 0
        val end = points.size

        for (i in 1..(end - 2)) {
            val d = perpendicularDistance(points[i], points[0], points[end - 1])
            if (d > dmax) {
                index = i
                dmax = d
            }
        }
        // If max distance is greater than epsilon, recursively simplify
        return if (dmax > epsilon) {
            // Recursive call
            val recResults1: List<LatLng> =
                simplifyPolyline(points.subList(0, index + 1), epsilon)
            val recResults2: List<LatLng> = simplifyPolyline(points.subList(index, end), epsilon)

            // Build the result list
            listOf(
                recResults1.subList(0, recResults1.lastIndex),
                recResults2,
            ).flatMap { it.toList() }
        } else {
            listOf(points[0], points[end - 1])
        }
    }

    // calculate perpendicular distance.
    private fun perpendicularDistance(
        pt: LatLng,
        lineFrom: LatLng,
        lineTo: LatLng,
    ): Double {
        val numerator =
            abs(
                (lineTo.longitude - lineFrom.longitude) * (lineFrom.latitude - pt.latitude) -
                    (lineFrom.longitude - pt.longitude) * (lineTo.latitude - lineFrom.latitude),
            )
        val denominator =
            sqrt(
                (lineTo.longitude - lineFrom.longitude).pow(2.0) +
                    (lineTo.latitude - lineFrom.latitude).pow(2.0),
            )

        return if (denominator == 0.0) {
            // Handle division by zero or near-zero denominator
            Double.POSITIVE_INFINITY
        } else {
            numerator / denominator
        }
    }
}
