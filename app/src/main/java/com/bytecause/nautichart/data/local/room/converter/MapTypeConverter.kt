package com.bytecause.nautichart.data.local.room.converter

import androidx.room.TypeConverter

class MapTypeConverter {
    @TypeConverter
    fun fromString(value: String): Map<String, String> {
        // Convert the string representation from the database to a Map
        // You can use a JSON parser or any other method based on your data format
        // For simplicity, this example uses a basic approach (comma-separated key-value pairs)
        val entries = value.split(",")
        val map = mutableMapOf<String, String>()

        for (entry in entries) {
            val keyValue = entry.split("=")
            if (keyValue.size == 2) {
                map[keyValue[0]] = keyValue[1]
            }
        }

        return map
    }

    @TypeConverter
    fun toString(value: Map<String, String>): String {
        // Convert the Map to its string representation for storage in the database
        // You can use a JSON serializer or any other method based on your data format
        // For simplicity, this example uses a basic approach (comma-separated key-value pairs)
        return value.entries.joinToString(",") { "${it.key}=${it.value}" }
    }
}
