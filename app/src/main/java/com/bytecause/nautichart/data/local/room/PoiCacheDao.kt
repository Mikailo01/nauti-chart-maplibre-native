package com.bytecause.nautichart.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
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

    @Query("SELECT EXISTS(SELECT 1 FROM poi_cache WHERE placeId = :placeId LIMIT 1)")
    fun isPlaceCached(placeId: Long): Flow<Boolean>
}