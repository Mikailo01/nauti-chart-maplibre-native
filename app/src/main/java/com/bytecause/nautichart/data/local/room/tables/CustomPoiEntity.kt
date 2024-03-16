package com.bytecause.nautichart.data.local.room.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_poi",
    foreignKeys = [
        ForeignKey(
            entity = CustomPoiCategoryEntity::class,
            parentColumns = ["categoryName"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomPoiEntity(
    @PrimaryKey(autoGenerate = true)
    val poiId: Long = 0,
    val poiName: String = "",
    @ColumnInfo(index = true) val categoryName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val drawableResourceName: String? = null
)