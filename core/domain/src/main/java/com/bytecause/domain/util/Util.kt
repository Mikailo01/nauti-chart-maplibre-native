package com.bytecause.domain.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object Util {
    fun timestampStringToTimestampLong(timestampString: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        format.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            val date: Date = format.parse(timestampString) ?: throw IllegalArgumentException("Invalid date format")
            date.time
        } catch (e: ParseException) {
            e.printStackTrace()
            -1
        }
    }

    // types of objects that should be queried for the entire region
    val searchTypesStringList =
        listOf("shop", "amenity", "leisure", "tourism", "seamark:type", "public_transport")

    // types of objects that should be omitted in the query for the entire region
    val excludeAmenityObjectsFilterList = listOf(
        "clock",
        "fixme",
        "smoking_area",
        "internet_kiosk"
    )
}