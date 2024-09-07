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
}