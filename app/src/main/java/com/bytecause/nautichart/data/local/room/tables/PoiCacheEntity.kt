package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.nautichart.data.local.room.converter.MapTypeConverter

@Entity(tableName = "poi_cache")
@TypeConverters(MapTypeConverter::class)
data class PoiCacheEntity(
    @PrimaryKey val placeId: Long = 0,
    val category: String = "",
    val drawableResourceName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = mapOf()
)