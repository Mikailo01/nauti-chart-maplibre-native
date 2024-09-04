package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.HarboursMetadataDatasetDao
import com.bytecause.data.mappers.asHarboursMetadataDatasetEntity
import com.bytecause.data.mappers.asHarboursMetadataDatasetModel
import com.bytecause.domain.abstractions.HarboursMetadataDatasetRepository
import com.bytecause.domain.model.HarboursMetadataDatasetModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HarboursMetadataDatasetRepositoryImpl(
    private val harboursMetadataDatasetDao: HarboursMetadataDatasetDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HarboursMetadataDatasetRepository {

    override suspend fun insertDataset(dataset: HarboursMetadataDatasetModel) {
        withContext(coroutineDispatcher) {
            harboursMetadataDatasetDao.insertDataset(dataset.asHarboursMetadataDatasetEntity())
        }
    }

    override suspend fun deleteDataset() {
        withContext(coroutineDispatcher) {
            harboursMetadataDatasetDao.deleteDataset()
        }
    }

    override fun getDataset(): Flow<HarboursMetadataDatasetModel?> =
        harboursMetadataDatasetDao.getDataset()
            .map { it?.asHarboursMetadataDatasetModel() }
            .flowOn(coroutineDispatcher)
}