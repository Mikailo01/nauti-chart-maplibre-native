package com.bytecause.nautichart.data.local.room.tables.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity

data class CategoryWithCustomPois(
    @Embedded val category: CustomPoiCategoryEntity,
    @Relation(
        parentColumn = "categoryName",
        entityColumn = "categoryName"
    )
    val pois: List<CustomPoiEntity>
)