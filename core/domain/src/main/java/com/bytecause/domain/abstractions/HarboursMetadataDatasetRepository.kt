package com.bytecause.domain.abstractions

import com.bytecause.domain.model.HarboursMetadataDatasetModel
import kotlinx.coroutines.flow.Flow

interface HarboursMetadataDatasetRepository {
    suspend fun insertDataset(dataset: HarboursMetadataDatasetModel)
    suspend fun deleteDataset()
    fun getDataset(): Flow<HarboursMetadataDatasetModel?>
}