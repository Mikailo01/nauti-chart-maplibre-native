package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.RadiusPoiMetadataDatasetDao
import com.bytecause.data.mappers.asRadiusPoiMetadataDatasetEntity
import com.bytecause.data.mappers.asRadiusPoiMetadataDatasetModel
import com.bytecause.domain.abstractions.RadiusPoiMetadataDatasetRepository
import com.bytecause.domain.model.RadiusPoiMetadataDatasetModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RadiusPoiMetadataDatasetRepositoryImpl(
    private val radiusPoiMetadataDatasetDao: RadiusPoiMetadataDatasetDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RadiusPoiMetadataDatasetRepository {

    override suspend fun insertDataset(dataset: RadiusPoiMetadataDatasetModel) {
        withContext(coroutineDispatcher) {
            radiusPoiMetadataDatasetDao.insertDataset(dataset.asRadiusPoiMetadataDatasetEntity())
        }
    }

    override suspend fun deleteDataset(categoryName: String) {
        withContext(coroutineDispatcher) {
            radiusPoiMetadataDatasetDao.deleteDatasetByName(categoryName)
        }
    }

    override fun getDatasetByName(categoryName: String): Flow<RadiusPoiMetadataDatasetModel?> =
        radiusPoiMetadataDatasetDao.getDatasetByName(categoryName)
            .map { it?.asRadiusPoiMetadataDatasetModel() }
            .flowOn(coroutineDispatcher)
}