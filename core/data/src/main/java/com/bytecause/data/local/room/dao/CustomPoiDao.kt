package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.relations.CategoryWithCustomPois
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPoiDao {

    @Query("SELECT * FROM custom_poi")
    fun loadAllCustomPoi(): Flow<List<CustomPoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPoi(poi: CustomPoiEntity)

    @Query("DELETE FROM custom_poi WHERE poiId = :id")
    suspend fun removeCustomPoi(id: Long)

    @Query("SELECT * FROM custom_poi WHERE poiId = :id")
    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity>

    @Transaction
    @Query("SELECT * FROM custom_poi_category WHERE categoryName = :categoryName")
    fun getCategoryWithPois(categoryName: String): Flow<CategoryWithCustomPois>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CustomPoiCategoryEntity)

    @Insert
    suspend fun insertCategories(categories: List<CustomPoiCategoryEntity>)

    @Query("SELECT * FROM custom_poi_category")
    fun getAllCategories(): Flow<List<CustomPoiCategoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_poi_category WHERE categoryName = :name LIMIT 1)")
    fun isCategoryNamePresent(name: String): Flow<Boolean>

    @Delete
    suspend fun removeCategory(category: CustomPoiCategoryEntity)
}