package com.bytecause.nautichart.coordinateconversions

import com.bytecause.map.util.MapUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CoordinatesValidationTest(
    private val coordinates: String,
    private val isValid: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("42°24'12\"N 16°35'52\"E", true),
                arrayOf("42°24'12\"N, 16°35'52\"E", true),
                arrayOf("32°14'40\"N 19°20'34\"E", true),
                arrayOf("20°42'12\"S, 50°30'55\"W", true),
                arrayOf("20°42'12\"S 50°30'55\"W", true),
                arrayOf("90°0'0\"S 180°0'0\"W", true),
                arrayOf("65°24'12.5\"N, 16°35'52.5\"E", true),

                arrayOf("65.4°24'12.5\"N, 16.5°35'52.5\"E", false),
                arrayOf("65.4°24'12.5\"N, 16°35'52.5\"E", false),
                arrayOf("92°24'12\"N, 16°35'52\"E", false),
                arrayOf("30°24'12\"N 190°35'52\"E", false),
                arrayOf("30°24'12\"H, 190°35'52\"E", false),
                arrayOf("30°24'12\"H, 180°35'52\"E", false),
                arrayOf("90°24'12\"H, 60°35'52\"E", false)
            )
        }
    }

    @Test
    fun areCoordinatesValidTest() {
        val result = com.bytecause.map.util.MapUtil.areCoordinatesValid(coordinates)
        assertEquals(result, isValid)
    }


}