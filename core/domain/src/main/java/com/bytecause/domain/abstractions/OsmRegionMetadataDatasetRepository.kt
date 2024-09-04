package com.bytecause.domain.abstractions

import com.bytecause.domain.model.OsmRegionMetadataDatasetModel
import kotlinx.coroutines.flow.Flow

interface OsmRegionMetadataDatasetRepository {
    suspend fun insertDataset(dataset: OsmRegionMetadataDatasetModel)
    suspend fun deleteDataset(regionId: Int)
    fun getDataset(regionId: Int): Flow<OsmRegionMetadataDatasetModel?>
    fun getAllDatasets(): Flow<List<OsmRegionMetadataDatasetModel?>>
}