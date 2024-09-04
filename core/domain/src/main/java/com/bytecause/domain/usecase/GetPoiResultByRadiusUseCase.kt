package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.model.OverpassNodeModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.PoiQueryModel
import com.bytecause.domain.util.PoiTagsUtil.extractCategoryFromPoiEntity
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.domain.util.distanceTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetPoiResultByRadiusUseCase(
    private val radiusPoiCacheRepository: RadiusPoiCacheRepository,
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

    operator fun invoke(entity: PoiQueryModel): Flow<ApiResult<List<PoiCacheModel>>> {
        return flow<ApiResult<List<PoiCacheModel>>> {
            radiusPoiCacheRepository.loadByCategory(entity.category).firstOrNull()
                ?.let { poiEntityList ->
                    val cachedPoiList = poiEntityList.filter { poiElement ->
                        entity.position.distanceTo(
                            LatLngModel(poiElement.latitude, poiElement.longitude),
                        ) <= entity.radius && isMatch(poiElement, entity.appliedFilters)
                    }.takeIf { it.isNotEmpty() && it.size >= 15 }

                    when {
                        cachedPoiList != null -> emit(ApiResult.Success(data = cachedPoiList))
                        else -> {
                            // make api call
                            overpassRepository.makeQuery<OverpassNodeModel>(query = entity.query)
                                .collect { result ->
                                    when {
                                        result.exception != null -> emit(ApiResult.Failure(result.exception))
                                        result.data?.first != null && result.data.second.isEmpty() -> {

                                        }

                                        else -> {
                                            result.data?.second?.let { data ->
                                                data.filter { element ->
                                                    radiusPoiCacheRepository.isPlaceCached(element.id)
                                                        .firstOrNull() == false
                                                }.takeIf { it.isNotEmpty() }?.map {
                                                    // extract POI category from tags
                                                    val category =
                                                        extractCategoryFromPoiEntity(it.tags)
                                                            .takeIf { category -> !category.isNullOrEmpty() }
                                                            .let { tagValue ->
                                                                formatTagString(
                                                                    tagValue
                                                                )
                                                            } ?: ""

                                                    PoiCacheModel(
                                                        placeId = it.id,
                                                        category = category,
                                                        // can't access android resources in platform-agnostic domain module
                                                        drawableResourceName = "",
                                                        latitude = it.lat,
                                                        longitude = it.lon,
                                                        tags = it.tags,
                                                    )
                                                }?.let { poiCacheEntity ->
                                                    radiusPoiCacheRepository.cacheResult(poiCacheEntity)
                                                    emit(ApiResult.Success(poiCacheEntity))
                                                } ?: run {
                                                    // result returned by api is already cached, emit this result.
                                                    data.map {
                                                        val category =
                                                            extractCategoryFromPoiEntity(it.tags)
                                                                .takeIf { category -> !category.isNullOrEmpty() }
                                                                .let { tagValue ->
                                                                    formatTagString(
                                                                        tagValue
                                                                    )
                                                                }
                                                                ?: ""

                                                        PoiCacheModel(
                                                            placeId = it.id,
                                                            category = category,
                                                            // can't access android resources in platform-agnostic domain module
                                                            drawableResourceName = "",
                                                            latitude = it.lat,
                                                            longitude = it.lon,
                                                            tags = it.tags,
                                                        )
                                                    }.let {
                                                        emit(ApiResult.Success(data = it))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
        }
            .flowOn(coroutineDispatcher)
    }
}
