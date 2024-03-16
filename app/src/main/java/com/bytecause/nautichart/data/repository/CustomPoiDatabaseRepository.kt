package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.CustomPoiDao
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.data.local.room.tables.relations.CategoryWithCustomPois
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CustomPoiDatabaseRepository @Inject constructor(
    private val customPoiDao: CustomPoiDao//,
    // private val customPoiCategoryDao: CustomPoiCategoryDao
) {

    val loadAllCustomPoi: Flow<List<CustomPoiEntity>> = customPoiDao.loadAllCustomPoi()
        .flowOn(Dispatchers.IO)

    suspend fun insertCategory(category: CustomPoiCategoryEntity) = withContext(Dispatchers.IO) {
        customPoiDao.insertCategory(category)
    }

    suspend fun insertCustomPoi(entity: CustomPoiEntity) = withContext(Dispatchers.IO) {
        customPoiDao.insertCustomPoi(entity)
    }

    fun getAllCategories(): Flow<List<CustomPoiCategoryEntity>> =
        customPoiDao.getAllCategories()
            .flowOn(Dispatchers.IO)

    fun getCategoryWithPois(categoryName: String): Flow<CategoryWithCustomPois> =
        customPoiDao.getCategoryWithPois(categoryName)
            .flowOn(Dispatchers.IO)

    fun isCategoryNamePresent(name: String): Flow<Boolean> =
        customPoiDao.isCategoryNamePresent(name)
            .flowOn(Dispatchers.IO)

    suspend fun removeCustomPoi(id: Long) = withContext(Dispatchers.IO) {
        customPoiDao.removeCustomPoi(id)
    }

    suspend fun removeCategory(category: CustomPoiCategoryEntity) = withContext(Dispatchers.IO) {
        customPoiDao.removeCategory(category)
    }

    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity> = customPoiDao.searchCustomPoiById(id)
        .flowOn(Dispatchers.IO)
}