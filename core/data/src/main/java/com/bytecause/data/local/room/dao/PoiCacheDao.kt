package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.PoiCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheResult(result: List<PoiCacheEntity>)

    @Query("DELETE FROM poi_cache")
    suspend fun clearCache()

    @Query("SELECT * FROM poi_cache")
    fun loadCache(): Flow<List<PoiCacheEntity>>

    @Query("SELECT DISTINCT category FROM poi_cache")
    fun getAllDistinctCategories(): Flow<List<String>>

    @Query("SELECT (SELECT COUNT(*) FROM poi_cache) == 0")
    fun isCacheEmpty(): Flow<Boolean>

    @Query("SELECT * FROM poi_cache WHERE placeId IN (:placeIds)")
    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheEntity>>

    @Query("SELECT * FROM poi_cache WHERE placeId = :id")
    fun searchPoiWithInfoById(id: Long): Flow<PoiCacheEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM poi_cache WHERE placeId = :placeId LIMIT 1)")
    fun isPlaceCached(placeId: Long): Flow<Boolean>

    @Query("SELECT * FROM poi_cache WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon AND category IN (:selectedCategories)")
    fun loadPoiCacheByBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        selectedCategories: Set<String>
    ): Flow<List<PoiCacheEntity>>
}