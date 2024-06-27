package com.bytecause.data.local.room.tables

import androidx.room.Embedded
import androidx.room.Relation

data class ContinentCountriesRelation(
    @Embedded val continentEntity: ContinentEntity = ContinentEntity(),
    @Relation(
        parentColumn = "id",
        entityColumn = "continentId"
    )
    val countries: List<CountryEntity> = emptyList()
)