package com.bytecause.util.common

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit


fun getDateTimeFromTimestamp(timestamp: Long): String {
    val timestampInstant = Instant.ofEpochMilli(timestamp)
    val articlePublishedZonedTime =
        ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

    return articlePublishedZonedTime.format(dateFormatter)
}

fun formatDuration(durationMillis: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays(durationMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
}