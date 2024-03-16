package com.bytecause.nautichart.data.local.room.tables.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bytecause.nautichart.data.local.room.tables.Country
import com.bytecause.nautichart.data.local.room.tables.Region

data class CountryRegions(
    @Embedded val country: Country = Country(),
    @Relation(
        parentColumn = "id",
        entityColumn = "countryId"
    )
    val regions: List<Region> = listOf()
)