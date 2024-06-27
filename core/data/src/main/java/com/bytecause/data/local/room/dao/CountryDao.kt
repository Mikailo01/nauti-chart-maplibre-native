package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Transaction
    @Query("SELECT * FROM continent WHERE id = :continentId")
    fun getContinentCountries(continentId: Int): Flow<ContinentCountriesRelation>
}