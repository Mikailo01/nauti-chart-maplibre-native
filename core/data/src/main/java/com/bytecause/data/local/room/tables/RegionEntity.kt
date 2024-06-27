package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.data.local.room.converter.MapTypeConverter

@Entity(tableName = "region")
@TypeConverters(MapTypeConverter::class)
data class RegionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val names: Map<String, String> = emptyMap(),
    val countryId: Int = 1
)