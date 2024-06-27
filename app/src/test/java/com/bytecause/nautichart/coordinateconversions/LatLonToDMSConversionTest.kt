package com.bytecause.nautichart.coordinateconversions

import com.bytecause.map.util.MapUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LatLonToDMSConversionTest(
    private val inputDecimal: Double,
    private val expectedDMS: String,
    private val conversionType: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(42.403333, "42°24'12\"N", "latitudeToDMS"),
                arrayOf(32.244444, "32°14'40\"N", "latitudeToDMS"),
                arrayOf(-20.703333, "20°42'12\"S", "latitudeToDMS"),
                arrayOf(16.597778, "16°35'52\"E", "longitudeToDMS"),
                arrayOf(19.342778, "19°20'34\"E", "longitudeToDMS"),
                arrayOf(-50.515278, "50°30'55\"W", "longitudeToDMS")
            )
        }
    }

    @Test
    fun testCoordinateDMSConversion() {
        val resultDMS: String = when (conversionType) {
            "latitudeToDMS" -> com.bytecause.map.util.MapUtil.latitudeToDMS(inputDecimal)
            "longitudeToDMS" -> com.bytecause.map.util.MapUtil.longitudeToDMS(inputDecimal)
            else -> throw IllegalArgumentException("Invalid conversion type: $conversionType")
        }

        assertEquals(expectedDMS, resultDMS)
    }
}