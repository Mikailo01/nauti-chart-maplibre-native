package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vessels_metadata_dataset",
    indices = [Index(value = ["id"], unique = true)]
)
data class VesselsMetadataDatasetEntity(
    @PrimaryKey val id: Int = 0,
    val timestamp: Long
)