package com.bytecause.nautichart.util

import com.bytecause.nautichart.domain.model.OpeningHour
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OpeningHoursChecker {

    fun isOpenNow(openingHours: List<OpeningHour>): Boolean {
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val currentHourMinute = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

        for (openingHour in openingHours) {
            if (openingHour.days.contains("-")) {
                val daysArray = openingHour.days.split("-")

                if (currentDayOfWeek in getDayOfWeek(daysArray[0])..getDayOfWeek(daysArray[1])) {
                    val hoursArray = openingHour.hours.split("-")
                    val openingTime = hoursArray[0]
                    val closingTime = hoursArray[1]
                    if (isCurrentTimeBetween(openingTime, closingTime, currentHourMinute)) {
                        return true
                    }
                }
            }
            else {
                val day = openingHour.days
                if (currentDayOfWeek == getDayOfWeek(day)) {
                    val hoursArray = openingHour.hours.split("-")
                    val openingTime = hoursArray[0]
                    val closingTime = hoursArray[1]
                    if (isCurrentTimeBetween(openingTime, closingTime, currentHourMinute)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun isCurrentTimeBetween(startTime: String, endTime: String, currentTime: String): Boolean {
        return currentTime in startTime..endTime
    }

    private fun getDayOfWeek(day: String): Int {
        return when (day.lowercase(Locale.getDefault())) {
            "mo" -> Calendar.MONDAY
            "tu" -> Calendar.TUESDAY
            "we" -> Calendar.WEDNESDAY
            "th" -> Calendar.THURSDAY
            "fr" -> Calendar.FRIDAY
            "sa" -> Calendar.SATURDAY
            "su" -> Calendar.SUNDAY
            else -> -1
        }
    }

}