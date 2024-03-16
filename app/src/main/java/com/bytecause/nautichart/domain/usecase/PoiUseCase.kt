package com.bytecause.nautichart.domain.usecase

import android.util.Log
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import com.bytecause.nautichart.data.repository.OverpassRepository
import com.bytecause.nautichart.data.repository.PoiCacheRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.OverpassNodeModel
import com.bytecause.nautichart.domain.model.PoiQueryEntity
import com.bytecause.nautichart.ui.util.DrawableUtil
import com.bytecause.nautichart.util.PoiUtil
import com.bytecause.nautichart.util.StringUtil
import com.bytecause.nautichart.util.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class PoiUseCase @Inject constructor(
    private val poiCacheRepository: PoiCacheRepository,
    private val overpassRepository: OverpassRepository
) {

    fun getPoiResultByRadius(entity: PoiQueryEntity): Flow<ApiResult<List<PoiCacheEntity>>> {
        return flow {
            poiCacheRepository.loadCachedResults.firstOrNull()?.let { poiEntityList ->
                poiEntityList.filter { poiElement ->
                    entity.category.contains(poiElement.category) && entity.position.distanceToAsDouble(
                        GeoPoint(poiElement.latitude, poiElement.longitude)
                    ) <= entity.radius // TODO("Create algorithm.")
                }.takeIf { it.isNotEmpty() && it.size >= 15 }?.let {
                    emit(ApiResult.Success(data = it))
                    return@flow
                } ?: run {
                    overpassRepository.makeQuery<OverpassNodeModel>(entity.query.getQuery()).also { result ->
                        if (result.exception != null) {
                            emit(ApiResult.Failure(result.exception))
                            return@flow
                        }
                        result.data?.filter { element ->
                            poiCacheRepository.isPlaceCached(element.id).firstOrNull() == false
                        }?.takeIf { it.isNotEmpty() }?.map {

                            val category = PoiUtil().extractCategoryFromPoiEntity(it.tags)
                                .takeIf { category -> !category.isNullOrEmpty() }
                                .let { tagValue -> StringUtil.formatTagString(tagValue) } ?: ""

                            PoiCacheEntity(
                                placeId = it.id,
                                category = category.also { Log.d(TAG(this), it) },
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
                        }
                    }
                }
            }
        }
    }

    fun getPoiResultByRegion(regionName: String, query: String): Flow<ApiResult<String>> {
        return flow {
            overpassRepository.makeQuery<OverpassNodeModel>(query).let { result ->
                if (result.exception == null && result.data != null) {
                    //val region = PoiUtil().extractRegionFromQuery(query)
                    result.data.map {
                        val category = PoiUtil().extractCategoryFromPoiEntity(it.tags)
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
    }

}