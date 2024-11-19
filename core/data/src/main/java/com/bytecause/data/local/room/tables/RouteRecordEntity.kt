package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.data.local.room.converter.PairTypeConverter
import com.bytecause.data.local.room.converter.SpeedFieldTypeConverter

@Entity(tableName = "route_record")
@TypeConverters(
    PairTypeConverter::class,
    SpeedFieldTypeConverter::class
)
data class RouteRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val distance: Double = 0.0,
    val startTime: Long = 0L,
    val dateCreated: Long = 0L,
    val points: List<Pair<Double, Double>> = emptyList(),
    val speed: Map<Pair<Double, Double>, Float> = emptyMap()
)