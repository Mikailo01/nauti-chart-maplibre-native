package com.bytecause.data.local.room.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vessel_info")
data class VesselInfoEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    val latitude: String = "",
    val longitude: String = "",
    val name: String = "",
    val type: String = "",
    val heading: String = "",
    val speed: String = "",
    val flag: String = "",
    val mmsi: String = "",
    val length: String = "",
    val eta: String = "",
    val timeStamp: Long = 0L
)
