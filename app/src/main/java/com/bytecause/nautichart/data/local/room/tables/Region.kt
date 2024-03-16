package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.nautichart.data.local.room.converter.MapTypeConverter

@Entity(tableName = "region")
@TypeConverters(MapTypeConverter::class)
data class Region(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val names: Map<String, String> = mapOf(),
    val countryId: Int = 1
)