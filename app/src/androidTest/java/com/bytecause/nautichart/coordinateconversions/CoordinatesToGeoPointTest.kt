package com.bytecause.nautichart.coordinateconversions

import com.bytecause.nautichart.util.MapUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.osmdroid.util.GeoPoint

@RunWith(Parameterized::class)
class CoordinatesToGeoPointTest(
    private val coordinates: String,
    private val expectedGeoPoint: GeoPoint
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("42°24'12\"N 16°35'52\"E", GeoPoint(42.403333, 16.597778)),
                arrayOf("32°14'40\"N 19°20'34\"E", GeoPoint(32.244444, 19.342778)),
                arrayOf("20°42'12\"S 50°30'55\"W", GeoPoint(-20.703333, -50.515278)),
                arrayOf("19°42'35\"S 49°19'43\"W", GeoPoint(-19.709829, -49.328613))
            )
        }
    }

    @Test
    fun coordinatesToGeoPointTest() {
        val result = MapUtil.stringCoordinatesToGeoPoint(coordinates)

        val delta = 0.0003

        assertNotNull(result)
        assertEquals(expectedGeoPoint.latitude, result!!.latitude, delta)
        assertEquals(expectedGeoPoint.longitude, result.longitude, delta)
    }
}