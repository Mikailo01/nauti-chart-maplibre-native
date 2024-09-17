package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.abstractions.RadiusPoiMetadataDatasetRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.model.OverpassNodeModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.PoiQueryModel
import com.bytecause.domain.model.RadiusPoiCacheModel
import com.bytecause.domain.model.RadiusPoiMetadataDatasetModel
import com.bytecause.domain.util.PoiTagsUtil.extractCategoryFromPoiEntity
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.domain.util.distanceTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private const val DISTANCE_THRESHOLD = 1000 // 1 KM

class GetPoiResultByRadiusUseCase(
    private val radiusPoiCacheRepository: RadiusPoiCacheRepository,
    private val radiusPoiMetadataDatasetRepository: RadiusPoiMetadataDatasetRepository,
    private val poiCacheRepository: PoiCacheRepository,
    private val overpassRepository: OverpassRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // check if elements returned by database match applied filters
    private fun isMatch(
        element: PoiCacheModel,
        filterTags: Map<String, List<String>>?,
    ): Boolean {
        if (filterTags == null) return true

        for ((key, value) in filterTags) {
            if (element.tags.keys.contains(key)) {
                if (value.contains(element.tags[key])) {
                    return true
                }
            }
        }
        return false
    }

    operator fun invoke(entity: PoiQueryModel): Flow<ApiResult<List<RadiusPoiCacheModel>>> = flow {
        val datasetCategoryName = entity.categoryList.joinToString()

        val radiusCache =
            radiusPoiMetadataDatasetRepository.getDatasetByName(datasetCategoryName)
                .firstOrNull()

        val poiCache = poiCacheRepository.getPoiByCategory(entity.categoryList).map { poiList ->
            poiList.filter { poiElement ->
                entity.position.distanceTo(
                    LatLngModel(
                        poiElement.latitude,
                        poiElement.longitude
                    ),
                ) <= entity.radius && isMatch(
                    poiElement,
                    entity.appliedFilters
                )
            }
                .takeIf { it.isNotEmpty() }
        }
            .firstOrNull()

        // if current search radius is bigger than in the dataset make new API call
        if (radiusCache == null || entity.radius > radiusCache.radius) {
            queryAndProcessResult(entity, datasetCategoryName, poiCache, radiusCache)
        } else {
            val isPositionInThreshold =
                LatLngModel(
                    latitude = radiusCache.latitude,
                    longitude = radiusCache.longitude
                ).distanceTo(entity.position) <= DISTANCE_THRESHOLD

            if (isPositionInThreshold) {
                radiusPoiCacheRepository.getPoiByCategory(entity.categoryList)
                    .firstOrNull()
                    ?.let { poiList ->

                        // filter POIs that are in current radius
                        val poiInRadius = poiList.filter {
                            LatLngModel(
                                it.latitude,
                                it.longitude
                            ).distanceTo(entity.position) <= entity.radius
                        }

                        emit(ApiResult.Success(poiInRadius))
                    }
            } else {
                queryAndProcessResult(entity, datasetCategoryName, poiCache, radiusCache)
            }
        }
    }
        .flowOn(coroutineDispatcher)

    private suspend fun FlowCollector<ApiResult<List<RadiusPoiCacheModel>>>.queryAndProcessResult(
        entity: PoiQueryModel,
        datasetCategoryName: String,
        poiCache: List<PoiCacheModel>?,
        radiusCache: RadiusPoiMetadataDatasetModel?
    ) {
        if (radiusCache != null) {
            // Since one-to-many relationship using foreign key is used, we have to delete this
            // dataset explicitly, because if we just replace this dataset then it's children
            // won't be deleted and there is no point of holding POIs that are out of our radius.
            radiusPoiMetadataDatasetRepository.deleteDataset(datasetCategoryName)
        }

        // this is flag which checks if dataset for given categories has been created
        var datasetCreated = false

        overpassRepository.makeQuery<OverpassNodeModel>(query = entity.query).collect { result ->
            when {
                result.exception != null -> handleException(poiCache, result.exception)
                result.data?.second.isNullOrEmpty().not() && result.data?.first == null -> {
                    result.data?.second?.let { data ->
                        val radiusPoiCacheModelList =
                            data.map {
                                // extract POI category from tags
                                val category =
                                    extractCategoryFromPoiEntity(it.tags)
                                        .takeIf { category -> !category.isNullOrEmpty() }
                                        .let { tagValue ->
                                            formatTagString(
                                                tagValue
                                            )
                                        } ?: ""

                                RadiusPoiCacheModel(
                                    placeId = it.id,
                                    category = category,
                                    latitude = it.lat,
                                    longitude = it.lon,
                                    tags = it.tags,
                                    datasetCategoryName = datasetCategoryName
                                )
                            }

                        if (!datasetCreated) {
                            radiusPoiMetadataDatasetRepository.insertDataset(
                                RadiusPoiMetadataDatasetModel(
                                    category = datasetCategoryName,
                                    latitude = entity.position.latitude,
                                    longitude = entity.position.longitude,
                                    radius = entity.radius,
                                    timestamp = System.currentTimeMillis()
                                )
                            )

                            datasetCreated = true
                        }

                        radiusPoiCacheRepository.cacheResult(radiusPoiCacheModelList)
                        emit(ApiResult.Success(radiusPoiCacheModelList))
                    }
                }
            }
        }
    }

    private suspend fun FlowCollector<ApiResult<List<RadiusPoiCacheModel>>>.handleException(
        poiCache: List<PoiCacheModel>?,
        exception: Throwable
    ) {
        if (poiCache != null) {
            emit(ApiResult.Success(poiCache.map {
                RadiusPoiCacheModel(
                    placeId = it.placeId,
                    category = it.category,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    tags = it.tags
                )
            }))
        } else {
            emit(ApiResult.Failure(exception))
        }
    }
}
