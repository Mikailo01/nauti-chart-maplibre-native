package com.bytecause.domain.abstractions

import com.bytecause.domain.model.VesselsMetadataDatasetModel
import kotlinx.coroutines.flow.Flow

interface VesselsMetadataDatasetRepository {
    suspend fun insertDataset(dataset: VesselsMetadataDatasetModel)
    suspend fun deleteDataset()
    fun getDataset(): Flow<VesselsMetadataDatasetModel?>
}