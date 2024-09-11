package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import com.bytecause.data.local.room.tables.ContinentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContinentDao {

    @Transaction
    @Query("SELECT * FROM continent WHERE id = :continentId")
    fun getContinentCountries(continentId: Int): Flow<ContinentCountriesRelation>

    @Query("SELECT * FROM continent")
    fun getAllContinents(): Flow<List<ContinentEntity>>
}