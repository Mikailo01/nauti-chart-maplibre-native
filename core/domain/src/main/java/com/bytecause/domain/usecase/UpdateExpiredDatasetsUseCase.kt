package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.HarboursMetadataDatasetRepository
import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.abstractions.RegionRepository
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.DatasetType
import com.bytecause.domain.model.OsmRegionMetadataDatasetModel
import com.bytecause.domain.model.UpdateResult
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.domain.util.Util.excludeAmenityObjectsFilterList
import com.bytecause.domain.util.Util.searchTypesStringList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class UpdateExpiredDatasetsUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val osmRegionMetadataDatasetRepository: OsmRegionMetadataDatasetRepository,
    private val harboursMetadataDatasetRepository: HarboursMetadataDatasetRepository,
    private val regionRepository: RegionRepository,
    private val getPoiResultByRegionUseCase: GetPoiResultByRegionUseCase,
    private val updateHarboursUseCase: UpdateHarboursUseCase
) {

    operator fun invoke(): Flow<UpdateResult> = flow {
        val harboursUpdateInterval = userPreferencesRepository.getHarboursUpdateInterval().first()
        val regionPoiUpdateInterval = userPreferencesRepository.getPoiUpdateInterval().first()
        val currentTime = System.currentTimeMillis()

        val harboursDataset =
            harboursMetadataDatasetRepository.getDataset().firstOrNull()

        val regionPoiDatasetList: List<OsmRegionMetadataDatasetModel>? =
            osmRegionMetadataDatasetRepository.getAllDatasets().firstOrNull()

        val shouldUpdateHarbours: Boolean = if (harboursDataset == null) false else {
            (currentTime - harboursDataset.timestamp) >= harboursUpdateInterval
        }

        val expiredRegionPoiDatasetList: List<OsmRegionMetadataDatasetModel> =
            regionPoiDatasetList?.filter { dataset -> (currentTime - dataset.timestamp) >= regionPoiUpdateInterval }
                ?: emptyList()

        if (expiredRegionPoiDatasetList.isNotEmpty()) {
            expiredRegionPoiDatasetList.forEach { regionDataset ->
                val regionName = regionRepository.getRegion(regionDataset.id).first().names["name"]

                regionName?.let { name ->
                    val query = OverpassQueryBuilder
                        .format(OverpassQueryBuilder.FormatTypes.JSON)
                        .timeout(240)
                        .region(name)
                        .type(OverpassQueryBuilder.Type.Node)
                        .search(
                            com.bytecause.domain.util.SearchTypes.UnionSet(searchTypesStringList)
                                .filterNot(
                                    emptyList(),
                                    excludeAmenityObjectsFilterList,
                                    emptyList(),
                                    emptyList(),
                                    emptyList(),
                                    emptyList()
                                )
                        )
                        .build()

                    emit(UpdateResult.UpdateStarted(DatasetType.RegionPoi(name)))

                    getPoiResultByRegionUseCase(
                        query = query,
                        regionId = regionDataset.id
                    ).collect { result ->
                        when (result) {
                            is ApiResult.Failure -> {
                                result.exception?.let { exception ->
                                    emit(UpdateResult.UpdateFailed(exception))
                                }
                            }

                            is ApiResult.Progress -> {
                                result.progress?.let { progress ->
                                    emit(UpdateResult.UpdateProgress(progress))
                                }
                            }

                            is ApiResult.Success -> {
                                emit(UpdateResult.DatasetUpdateFinished)
                            }
                        }
                    }
                }
            }
        }

        if (shouldUpdateHarbours) {

            emit(UpdateResult.UpdateStarted(DatasetType.Harbours))

            updateHarboursUseCase(forceUpdate = true).collect { result ->
                when (result) {
                    is ApiResult.Failure -> {
                        result.exception?.let { exception ->
                            emit(UpdateResult.UpdateFailed(exception))
                        }
                    }

                    is ApiResult.Progress -> {
                        result.progress?.let { progress ->
                            emit(UpdateResult.UpdateProgress(progress))
                        }
                    }

                    is ApiResult.Success -> {
                        emit(UpdateResult.DatasetUpdateFinished)
                    }
                }
            }
        }

        emit(UpdateResult.DatasetsUpToDate)
    }
}