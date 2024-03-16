package com.bytecause.nautichart.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.nautichart.data.local.room.tables.relations.CountryRegions
import com.bytecause.nautichart.data.local.room.tables.Region
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {

    @Transaction
    @Query("SELECT * FROM country WHERE id = :countryId")
    fun getCountryRegions(countryId: Int): Flow<CountryRegions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRegions(regions: List<Region>)
}