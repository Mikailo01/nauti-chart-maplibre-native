package com.bytecause.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country")
data class CountryEntity(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val iso2: String = "",
    val iso3: String = "",
    val continentId: Int = 1
)