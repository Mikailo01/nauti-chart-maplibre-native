package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.data.local.room.converter.MapTypeConverter

@Entity(tableName = "radius_poi_cache")
@TypeConverters(MapTypeConverter::class)
data class RadiusPoiCacheEntity(
    @PrimaryKey val placeId: Long = 0,
    val category: String = "",
    val drawableResourceName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = emptyMap()
)