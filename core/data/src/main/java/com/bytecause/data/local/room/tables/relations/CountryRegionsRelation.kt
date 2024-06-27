package com.bytecause.data.local.room.tables.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bytecause.data.local.room.tables.CountryEntity
import com.bytecause.data.local.room.tables.RegionEntity

data class CountryRegionsRelation(
    @Embedded val countryEntity: CountryEntity = CountryEntity(),
    @Relation(
        parentColumn = "id",
        entityColumn = "countryId"
    )
    val regionEntities: List<RegionEntity> = emptyList()
)