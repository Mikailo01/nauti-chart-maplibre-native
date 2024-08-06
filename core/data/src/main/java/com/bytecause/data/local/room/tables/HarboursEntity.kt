package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.data.local.room.converter.MapTypeConverter


@Entity(tableName = "harbours")
@TypeConverters(MapTypeConverter::class)
data class HarboursEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: Map<String, String> = emptyMap()
)