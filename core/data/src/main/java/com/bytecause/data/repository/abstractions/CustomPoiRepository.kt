package com.bytecause.data.repository.abstractions

import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.relations.CategoryWithCustomPois
import kotlinx.coroutines.flow.Flow

interface CustomPoiRepository {
    fun loadAllCustomPoi(): Flow<List<CustomPoiEntity>>
    suspend fun insertCategory(category: CustomPoiCategoryEntity)
    suspend fun insertCustomPoi(entity: CustomPoiEntity)
    suspend fun removeCustomPoi(id: Long)
    suspend fun removeCategory(category: CustomPoiCategoryEntity)
    fun getAllCategories(): Flow<List<CustomPoiCategoryEntity>>
    fun getCategoryWithPois(categoryName: String): Flow<CategoryWithCustomPois>
    fun isCategoryNamePresent(name: String): Flow<Boolean>
    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity>
}