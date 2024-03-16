package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_poi_category",
    indices = [Index(value = ["categoryName"], unique = true)])
data class CustomPoiCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long = 0,
    val categoryName: String = "",
    val drawableResourceName: String = ""
)