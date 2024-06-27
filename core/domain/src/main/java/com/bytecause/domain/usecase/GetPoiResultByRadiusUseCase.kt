package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
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

    // TODO("Filter result returned by API to exclude undesired elements")
    operator fun invoke(entity: PoiQueryModel): Flow<ApiResult<List<PoiCacheModel>>> {
        return flow {
            poiCacheRepository.loadCachedResults().firstOrNull()?.let { poiEntityList ->
                poiEntityList.filter { poiElement ->
                    entity.category.contains(poiElement.category) && entity.position.distanceTo(
                        LatLngModel(poiElement.latitude, poiElement.longitude),
                    ) <= entity.radius && isMatch(poiElement, entity.appliedFilters)
                }.takeIf { it.isNotEmpty() && it.size >= 15 }?.let {
                    // if result from database is not empty and total count is at least 15 emit
                    // result from database, otherwise make api call
                    emit(ApiResult.Success(data = it))
                    return@flow
                } ?: run {
                    // make api call
                    overpassRepository.makeQuery<OverpassNodeModel>(query = entity.query)
                        .also { result ->
                            if (result.exception != null) {
                                emit(ApiResult.Failure(result.exception))
                                return@flow
                            }

                            result.data?.filter { element ->
                                poiCacheRepository.isPlaceCached(element.id).firstOrNull() == false
                            }?.takeIf { it.isNotEmpty() }?.map {
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
                                poiCacheRepository.cacheResult(poiCacheEntity)

                                // Filter out categories which don't belong to specified category set.
                                emit(
                                    ApiResult.Success(
                                        data = poiCacheEntity, // .filter { element -> entity.category.contains(element.category) }
                                    ),
                                )
                                return@flow
                            } ?: run {
                                // result returned by api is already cached, emit this result.
                                result.data?.map {
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
                                }?.let {
                                    emit(ApiResult.Success(data = it))
                                    return@flow
                                }
                            }
                        }
                }
            }
        }
            .flowOn(coroutineDispatcher)
    }
}
