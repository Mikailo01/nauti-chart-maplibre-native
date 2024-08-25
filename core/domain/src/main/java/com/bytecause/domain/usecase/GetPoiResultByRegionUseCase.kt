package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassNodeModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.util.PoiTagsUtil.extractCategoryFromPoiEntity
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetPoiResultByRegionUseCase(
    private val overpassRepository: OverpassRepository,
    private val poiCacheRepository: PoiCacheRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        regionName: String,
        query: String,
    ): Flow<ApiResult<String>> = flow<ApiResult<String>> {
        overpassRepository.makeQuery<OverpassNodeModel>(query).collect { result ->
            if (result.exception == null && result.data != null) {

                // val region = PoiUtil().extractRegionFromQuery(query)
                val entityList = result.data.map {
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
                    )
                }

                poiCacheRepository.cacheResult(entityList)
                emit(ApiResult.Progress(progress = entityList.size))
            } else if (result.exception != null) {
                emit(ApiResult.Failure(exception = result.exception))
            }
        }

        emit(ApiResult.Success(data = regionName))
    }
        .flowOn(coroutineDispatcher)
}
