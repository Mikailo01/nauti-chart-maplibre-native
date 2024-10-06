package com.bytecause.util.common

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun getDateTimeFromTimestamp(timestamp: Long): String {
    val timestampInstant = Instant.ofEpochMilli(timestamp)
    val articlePublishedZonedTime =
        ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

    return articlePublishedZonedTime.format(dateFormatter)
}