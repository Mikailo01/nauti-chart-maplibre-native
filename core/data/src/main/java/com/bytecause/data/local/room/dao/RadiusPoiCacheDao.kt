package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.RadiusPoiCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RadiusPoiCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheResult(result: List<RadiusPoiCacheEntity>)

    @Query("SELECT * FROM radius_poi_cache WHERE placeId IN (:placeIds)")
    fun searchInCache(placeIds: List<Long>): Flow<List<RadiusPoiCacheEntity>>

    @Query("SELECT * FROM radius_poi_cache WHERE category IN (:category)")
    fun getPoiByCategory(category: List<String>): Flow<List<RadiusPoiCacheEntity>>

    @Query("SELECT * FROM radius_poi_cache WHERE placeId = :id")
    fun searchPoiWithInfoById(id: Long): Flow<RadiusPoiCacheEntity?>
}