package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.data.local.room.tables.RegionEntity
import com.bytecause.data.local.room.tables.relations.CountryRegionsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {

    @Transaction
    @Query("SELECT * FROM country WHERE id = :countryId")
    fun getCountryRegions(countryId: Int): Flow<CountryRegionsRelation>

    @Query("SELECT * FROM region WHERE isDownloaded = 1")
    fun getAllDownloadedRegions(): Flow<List<RegionEntity>>

    @Query("SELECT * FROM region WHERE id = :id")
    fun getRegionById(id: Int): Flow<RegionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRegions(regionEntities: List<RegionEntity>)
}