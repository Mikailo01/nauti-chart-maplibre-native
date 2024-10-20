package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anchorage_movement_track")
data class AnchorageMovementTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)