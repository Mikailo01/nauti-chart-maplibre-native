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

    @Query("DELETE FROM radius_poi_cache")
    suspend fun clearCache()

    @Query("SELECT * FROM radius_poi_cache WHERE category IN (:category)")
    fun loadByCategory(category: List<String>): Flow<List<RadiusPoiCacheEntity>>

    @Query("SELECT DISTINCT category FROM radius_poi_cache")
    fun getAllDistinctCategories(): Flow<List<String>>

    @Query("SELECT (SELECT COUNT(*) FROM radius_poi_cache) == 0")
    fun isCacheEmpty(): Flow<Boolean>

    @Query("SELECT * FROM radius_poi_cache WHERE placeId IN (:placeIds)")
    fun searchInCache(placeIds: List<Long>): Flow<List<RadiusPoiCacheEntity>>

    @Query("SELECT * FROM radius_poi_cache WHERE placeId = :id")
    fun searchPoiWithInfoById(id: Long): Flow<RadiusPoiCacheEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM radius_poi_cache WHERE placeId = :placeId LIMIT 1)")
    fun isPlaceCached(placeId: Long): Flow<Boolean>

    @Query("SELECT * FROM radius_poi_cache WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon AND category IN (:selectedCategories)")
    fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<RadiusPoiCacheEntity>>
}