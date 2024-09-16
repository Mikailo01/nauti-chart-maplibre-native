package com.bytecause.domain.abstractions

import com.bytecause.domain.model.RadiusPoiMetadataDatasetModel
import kotlinx.coroutines.flow.Flow

interface RadiusPoiMetadataDatasetRepository {
    suspend fun insertDataset(dataset: RadiusPoiMetadataDatasetModel)
    suspend fun deleteDataset(categoryName: String)
    fun getDatasetByName(categoryName: String): Flow<RadiusPoiMetadataDatasetModel?>
}