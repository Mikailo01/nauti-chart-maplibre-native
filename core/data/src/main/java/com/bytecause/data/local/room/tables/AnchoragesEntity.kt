package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anchorages")
data class AnchoragesEntity(
    @PrimaryKey val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)