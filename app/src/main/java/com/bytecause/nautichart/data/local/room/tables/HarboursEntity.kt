package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harbours")
data class HarboursEntity(
    @PrimaryKey val harborId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val harborName: String = "",
    val url: String = "",
    val type: String = ""
)