package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.relations.CategoryWithCustomPois
import com.bytecause.data.repository.abstractions.CustomPoiRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CustomPoiRepositoryImpl @Inject constructor(
    private val customPoiDao: CustomPoiDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CustomPoiRepository {

    override fun loadAllCustomPoi(): Flow<List<CustomPoiEntity>> =
        customPoiDao.loadAllCustomPoi()
            .flowOn(coroutineDispatcher)

    override suspend fun insertCategory(category: CustomPoiCategoryEntity) =
        withContext(coroutineDispatcher) {
            customPoiDao.insertCategory(category)
        }

    override suspend fun insertCustomPoi(entity: CustomPoiEntity) =
        withContext(coroutineDispatcher) {
            customPoiDao.insertCustomPoi(entity)
        }

    override fun getAllCategories(): Flow<List<CustomPoiCategoryEntity>> =
        customPoiDao.getAllCategories()
            .flowOn(coroutineDispatcher)

    override fun getCategoryWithPois(categoryName: String): Flow<CategoryWithCustomPois> =
        customPoiDao.getCategoryWithPois(categoryName)
            .flowOn(coroutineDispatcher)

    override fun isCategoryNamePresent(name: String): Flow<Boolean> =
        customPoiDao.isCategoryNamePresent(name)
            .flowOn(coroutineDispatcher)

    override suspend fun removeCustomPoi(id: Long) = withContext(coroutineDispatcher) {
        customPoiDao.removeCustomPoi(id)
    }

    override suspend fun removeCategory(category: CustomPoiCategoryEntity) =
        withContext(coroutineDispatcher) {
            customPoiDao.removeCategory(category)
        }

    override fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity> =
        customPoiDao.searchCustomPoiById(id)
            .flowOn(coroutineDispatcher)
}