package com.bytecause.nautichart.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "continent")
data class Continent(
    @PrimaryKey val id: Int = 0,
    val name: String = ""
)