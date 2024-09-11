package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.OsmRegionMetadataDatasetDao
import com.bytecause.data.mappers.asOsmRegionMetadataDatasetEntity
import com.bytecause.data.mappers.asOsmRegionMetadataDatasetModel
import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.model.OsmRegionMetadataDatasetModel
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OsmRegionMetadataDatasetRepositoryImpl @Inject constructor(
    private val osmRegionMetadataDatasetDao: OsmRegionMetadataDatasetDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OsmRegionMetadataDatasetRepository {

    override suspend fun insertDataset(dataset: OsmRegionMetadataDatasetModel) {
        withContext(coroutineDispatcher) {
            osmRegionMetadataDatasetDao.insertDataset(dataset.asOsmRegionMetadataDatasetEntity())
        }
    }

    override suspend fun deleteDataset(regionId: Int) {
        withContext(coroutineDispatcher) {
            osmRegionMetadataDatasetDao.deleteDatasetById(regionId)
        }
    }

    override fun getDataset(regionId: Int): Flow<OsmRegionMetadataDatasetModel?> =
        osmRegionMetadataDatasetDao.getDatasetById(regionId)
            .map { it?.asOsmRegionMetadataDatasetModel() }
            .flowOn(coroutineDispatcher)

    override fun getAllDatasets(): Flow<List<OsmRegionMetadataDatasetModel>> =
        osmRegionMetadataDatasetDao.getAllDatasets()
            .map { originalList -> mapList(originalList) { it.asOsmRegionMetadataDatasetModel() } }
            .flowOn(coroutineDispatcher)

}