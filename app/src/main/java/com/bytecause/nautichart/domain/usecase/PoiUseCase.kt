package com.bytecause.nautichart.domain.usecase

import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import com.bytecause.nautichart.data.repository.OverpassRepository
import com.bytecause.nautichart.data.repository.PoiCacheRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.OverpassNodeModel
import com.bytecause.nautichart.domain.model.PoiQueryEntity
import com.bytecause.nautichart.ui.util.DrawableUtil
import com.bytecause.nautichart.util.PoiUtil
import com.bytecause.nautichart.util.StringUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class PoiUseCase @Inject constructor(
    private val poiCacheRepository: PoiCacheRepository,
    private val overpassRepository: OverpassRepository
) {

    // check if elements returned by database match applied filters
    private fun isMatch(element: PoiCacheEntity, filterTags: Map<String, List<String>>?): Boolean {
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
    fun getPoiResultByRadius(entity: PoiQueryEntity): Flow<ApiResult<List<PoiCacheEntity>>> {
        return flow {
            poiCacheRepository.loadCachedResults.firstOrNull()?.let { poiEntityList ->
                poiEntityList.filter { poiElement ->
                    entity.category.contains(poiElement.category) && entity.position.distanceToAsDouble(
                        GeoPoint(poiElement.latitude, poiElement.longitude)
                    ) <= entity.radius && isMatch(poiElement, entity.appliedFilters)
                }.takeIf { it.isNotEmpty() && it.size >= 15 }?.let {
                    // if result from database is not empty and total count is at least 15 emit
                    // result from database, otherwise make api call
                    emit(ApiResult.Success(data = it))
                    return@flow
                } ?: run {
                    // make api call
                    overpassRepository.makeQuery<OverpassNodeModel>(entity.query.getQuery())
                        .also { result ->
                            if (result.exception != null) {
                                emit(ApiResult.Failure(result.exception))
                                return@flow
                            }

                            result.data?.filter { element ->
                                poiCacheRepository.isPlaceCached(element.id).firstOrNull() == false
                            }?.takeIf { it.isNotEmpty() }?.map {

                                // extract POI category from tags
                                val category = PoiUtil.extractCategoryFromPoiEntity(it.tags)
                                    .takeIf { category -> !category.isNullOrEmpty() }
                                    .let { tagValue -> StringUtil.formatTagString(tagValue) } ?: ""

                                PoiCacheEntity(
                                    placeId = it.id,
                                    category = category,
                                    drawableResourceName = DrawableUtil.getResourceName(category),
                                    latitude = it.lat,
                                    longitude = it.lon,
                                    tags = it.tags
                                )
                            }?.let { poiCacheEntity ->
                                poiCacheRepository.cacheResult(poiCacheEntity)

                                // Filter out categories which don't belong to specified category set.
                                emit(ApiResult.Success(data = poiCacheEntity/*.filter { element -> entity.category.contains(element.category) }*/))
                                return@flow
                            } ?: run {
                                // result returned by api is already cached, emit this result.
                                result.data?.map {
                                    val category = PoiUtil.extractCategoryFromPoiEntity(it.tags)
                                        .takeIf { category -> !category.isNullOrEmpty() }
                                        .let { tagValue -> StringUtil.formatTagString(tagValue) }
                                        ?: ""

                                    PoiCacheEntity(
                                        placeId = it.id,
                                        category = category,
                                        drawableResourceName = DrawableUtil.getResourceName(category),
                                        latitude = it.lat,
                                        longitude = it.lon,
                                        tags = it.tags
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
            .flowOn(Dispatchers.Default)
    }

    fun getPoiResultByRegion(regionName: String, query: String): Flow<ApiResult<String>> {
        return flow {
            overpassRepository.makeQuery<OverpassNodeModel>(query).let { result ->
                if (result.exception == null && result.data != null) {
                    //val region = PoiUtil().extractRegionFromQuery(query)
                    result.data.map {
                        val category = PoiUtil.extractCategoryFromPoiEntity(it.tags)
                            .takeIf { category -> !category.isNullOrEmpty() }
                            .let { tagValue -> StringUtil.formatTagString(tagValue) } ?: ""

                        PoiCacheEntity(
                            placeId = it.id,
                            category = category,
                            drawableResourceName = DrawableUtil.getResourceName(category),
                            latitude = it.lat,
                            longitude = it.lon,
                            tags = it.tags
                        )
                    }.let { entity ->
                        poiCacheRepository.cacheResult(entity)
                        emit(ApiResult.Success(data = regionName))
                        return@flow
                    }
                } else if (result.exception != null) {
                    emit(ApiResult.Failure(exception = result.exception))
                    return@flow
                }
            }
        }
            .flowOn(Dispatchers.Default)
    }

}