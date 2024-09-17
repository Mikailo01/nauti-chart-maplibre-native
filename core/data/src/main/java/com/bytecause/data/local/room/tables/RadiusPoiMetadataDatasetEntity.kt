package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "radius_poi_metadata_dataset",
    indices = [Index(value = ["category"], unique = true)]
)
data class RadiusPoiMetadataDatasetEntity(
    @PrimaryKey val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Int = 0,
    val timestamp: Long = 0L
)