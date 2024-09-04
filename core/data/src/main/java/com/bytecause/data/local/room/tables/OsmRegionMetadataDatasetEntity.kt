package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "osm_region_metadata_dataset",
    indices = [Index(value = ["id"], unique = true)]
)
data class OsmRegionMetadataDatasetEntity(
    @PrimaryKey val id: Int = 0,
    val timestamp: String
)