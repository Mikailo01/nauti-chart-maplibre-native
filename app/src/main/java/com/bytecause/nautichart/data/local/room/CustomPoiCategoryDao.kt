package com.bytecause.nautichart.data.local.room

/*@Dao
interface CustomPoiCategoryDao {

    @Insert
    suspend fun insertCategory(category: CustomPoiCategoryEntity)

    @Insert
    suspend fun insertCategories(categories: List<CustomPoiCategoryEntity>)

    @Query("SELECT * FROM custom_poi_category")
    fun getAllCategories(): Flow<List<CustomPoiCategoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_poi_category WHERE categoryName = :name LIMIT 1)")
    fun isCategoryNamePresent(name: String): Flow<Boolean>

    @Delete
    suspend fun removeCategory(category: CustomPoiCategoryEntity)
}*/