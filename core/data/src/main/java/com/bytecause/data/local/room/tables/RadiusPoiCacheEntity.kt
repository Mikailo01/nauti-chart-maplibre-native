package com.bytecause.data.local.room.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.data.local.room.converter.MapTypeConverter

@Entity(
    tableName = "radius_poi_cache",
    foreignKeys = [androidx.room.ForeignKey(
        entity = RadiusPoiMetadataDatasetEntity::class,
        parentColumns = ["category"],
        childColumns = ["datasetCategoryName"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
@TypeConverters(MapTypeConverter::class)
data class RadiusPoiCacheEntity(
    @PrimaryKey val placeId: Long = 0,
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = emptyMap(),
    @ColumnInfo(index = true) val datasetCategoryName: String = ""
)