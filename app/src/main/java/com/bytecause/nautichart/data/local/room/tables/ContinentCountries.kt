package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Embedded
import androidx.room.Relation

data class ContinentCountries(
    @Embedded val continent: Continent = Continent(),
    @Relation(
        parentColumn = "id",
        entityColumn = "continentId"
    )
    val countries: List<Country> = listOf()
)