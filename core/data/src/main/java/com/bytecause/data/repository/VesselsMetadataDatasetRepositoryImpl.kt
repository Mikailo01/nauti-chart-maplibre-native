package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.VesselsMetadataDatasetDao
import com.bytecause.data.mappers.asVesselsMetadataDatasetEntity
import com.bytecause.data.mappers.asVesselsMetadataDatasetModel
import com.bytecause.domain.abstractions.VesselsMetadataDatasetRepository
import com.bytecause.domain.model.VesselsMetadataDatasetModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VesselsMetadataDatasetRepositoryImpl(
    private val vesselsMetadataDatasetDao: VesselsMetadataDatasetDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VesselsMetadataDatasetRepository {

    override suspend fun insertDataset(dataset: VesselsMetadataDatasetModel) {
        withContext(coroutineDispatcher) {
            vesselsMetadataDatasetDao.insertDataset(dataset.asVesselsMetadataDatasetEntity())
        }
    }

    override suspend fun deleteDataset() {
        withContext(coroutineDispatcher) {
            vesselsMetadataDatasetDao.deleteDataset()
        }
    }

    override fun getDataset(): Flow<VesselsMetadataDatasetModel?> =
        vesselsMetadataDatasetDao.getDataset()
            .map { it?.asVesselsMetadataDatasetModel() }
            .flowOn(coroutineDispatcher)
}