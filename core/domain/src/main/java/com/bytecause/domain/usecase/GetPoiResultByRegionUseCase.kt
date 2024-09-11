package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.RegionPoiCacheRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OsmRegionMetadataDatasetModel
import com.bytecause.domain.model.OverpassNodeModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.util.PoiTagsUtil.extractCategoryFromPoiEntity
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.domain.util.Util.timestampStringToTimestampLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetPoiResultByRegionUseCase(
    private val overpassRepository: OverpassRepository,
    private val regionPoiCacheRepository: RegionPoiCacheRepository,
    private val osmRegionMetadataDatasetRepository: OsmRegionMetadataDatasetRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(
        query: String,
        regionId: Int
    ): Flow<ApiResult<Nothing>> = flow<ApiResult<Nothing>> {
        overpassRepository.makeQuery<OverpassNodeModel>(query).collect { result ->
            if (result.exception == null && result.data != null) {
                when {
                    result.data.first != null && result.data.second.isEmpty() -> {
                        result.data.first?.let { dateString ->
                            osmRegionMetadataDatasetRepository.insertDataset(
                                OsmRegionMetadataDatasetModel(
                                    id = regionId,
                                    timestamp = timestampStringToTimestampLong(dateString)
                                )
                            )
                        }
                    }

                    else -> {
                        val entityList = result.data.second.map {
                            val category = extractCategoryFromPoiEntity(it.tags)
                                .takeIf { category -> !category.isNullOrEmpty() }
                                .let { tagValue -> formatTagString(tagValue) } ?: ""

                            PoiCacheModel(
                                placeId = it.id,
                                category = category,
                                // can't access android resources in platform-agnostic domain module
                                drawableResourceName = "",
                                latitude = it.lat,
                                longitude = it.lon,
                                tags = it.tags,
                                datasetId = regionId
                            )
                        }

                        regionPoiCacheRepository.cacheResult(entityList)
                        emit(ApiResult.Progress(progress = entityList.size))
                    }
                }
            } else if (result.exception != null) {
                emit(ApiResult.Failure(exception = result.exception))
            }
        }

        emit(ApiResult.Success(data = null))
    }
        .flowOn(coroutineDispatcher)
}
