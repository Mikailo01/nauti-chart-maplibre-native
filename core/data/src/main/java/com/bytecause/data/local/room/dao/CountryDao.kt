package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.bytecause.domain.model.CountryModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Query("SELECT * FROM country WHERE iso2 = :isoCode")
    fun getCountryByIso(isoCode: String): Flow<CountryModel>
}