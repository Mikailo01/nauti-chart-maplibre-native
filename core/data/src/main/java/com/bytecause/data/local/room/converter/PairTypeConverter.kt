package com.bytecause.data.local.room.converter

import androidx.room.TypeConverter

class PairTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Pair<Double, Double>> {
        val entries = value.split(",")
        val pairs = mutableListOf<Pair<Double, Double>>()

        for (entry in entries) {
            val stringPair = entry.split(";")

            if (stringPair.size == 2) {
                // Parse each pair of consecutive entries as a (Double, Double) pair
                val first = stringPair[0].toDouble()
                val second = stringPair[1].toDouble()
                pairs.add(first to second)
            }
        }

        return pairs
    }

    @TypeConverter
    fun toString(values: List<Pair<Double, Double>>): String {
        // Split first and second with semicolon
        return values.joinToString(",") { "${it.first};${it.second}" }
    }
}