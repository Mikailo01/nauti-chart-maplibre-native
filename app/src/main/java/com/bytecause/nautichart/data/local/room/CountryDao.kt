package com.bytecause.nautichart.data.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.nautichart.data.local.room.tables.ContinentCountries
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Transaction
    @Query("SELECT * FROM continent WHERE id = :continentId")
    fun getContinentCountries(continentId: Int): Flow<ContinentCountries>
}