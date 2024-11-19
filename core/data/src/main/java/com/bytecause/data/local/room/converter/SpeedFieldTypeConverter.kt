package com.bytecause.data.local.room.converter

import androidx.room.TypeConverter

class SpeedFieldTypeConverter {

    @TypeConverter
    fun fromString(value: String): Map<Pair<Double, Double>, Float> {
        val entries = value.split(",")
        val map = mutableMapOf<Pair<Double, Double>, Float>()

        for (entry in entries) {
            val keyValue = entry.split(";", "=")
            if (keyValue.size == 3) {
                map[keyValue[0].toDouble() to keyValue[1].toDouble()] = keyValue[2].toFloat()
            }
        }

        return map
    }

    @TypeConverter
    fun toString(value: Map<Pair<Double, Double>, Float>): String {
        return value.entries.joinToString(",") {
            "${it.key.first};${it.key.second}=${it.value}"
        }
    }
}