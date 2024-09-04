package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.RegionRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassRelationModel
import com.bytecause.domain.model.RegionModel
import com.bytecause.domain.util.OverpassQueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class GetRegionsUseCase(
    private val regionRepository: RegionRepository,
    private val overpassRepository: OverpassRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // filters out all unwanted objects
    private fun filterRegionObjects(
        objects: List<OverpassRelationModel>,
        countryId: Int,
        isoCode: String,
    ): List<RegionModel> {
        val supportedBoundaryList = listOf("historic", "administrative")

        val filteredObjects =
            objects.filter {
                !it.tags.keys.contains("ISO3166-1") &&
                        !it.tags.keys.contains("addr:city") &&
                        it.tags["boundary"] in supportedBoundaryList &&
                        if (it.tags["ISO3166-2"] == null) {
                            true
                        } else {
                            it.tags["ISO3166-2"]?.startsWith(
                                isoCode,
                            ) == true
                        }
            }

        if (filteredObjects.isEmpty()) {
            return listOf(
                RegionModel(
                    names =
                    mapOf(
                        "name" to
                                Locale(
                                    Locale.getDefault().isO3Country,
                                    isoCode,
                                ).displayCountry,
                    ),
                    countryId = countryId,
                ),
            )
        }

        return filteredObjects.map {
            RegionModel(
                names = it.tags.filter { tag -> tag.key.startsWith("name") },
                countryId = countryId,
            )
        }
    }

    operator fun invoke(
        countryId: Int,
        isoCode: String,
        query: String,
    ): Flow<ApiResult<List<RegionModel>>> = flow {
        var filteredList: List<RegionModel> = emptyList()
        // if regions are cached in the database emit results and return flow, otherwise make query
        // to obtain them
        regionRepository.getRegions(countryId)
            .firstOrNull()?.regionModels?.takeIf { it.isNotEmpty() }?.let {
                emit(ApiResult.Success(data = it))
                return@flow
            } ?: run {
            overpassRepository.makeQuery<OverpassRelationModel>(query = query, getTimestamp = false)
                .firstOrNull()?.let { result ->
                when {
                    result.exception == null && !result.data?.toList().isNullOrEmpty() -> {
                        filteredList =
                            filterRegionObjects(
                                result.data?.second ?: emptyList(),
                                countryId,
                                isoCode,
                            ).takeIf { it.isNotEmpty() } ?: run {
                                // If list is empty, do another query with adminLevel 6 (original query uses adminLevel 4)
                                val newResult =
                                    overpassRepository.makeQuery<OverpassRelationModel>(
                                        OverpassQueryBuilder
                                            .format(OverpassQueryBuilder.FormatTypes.JSON)
                                            .timeout(90)
                                            .geocodeAreaISO(isoCode)
                                            .type(OverpassQueryBuilder.Type.Relation)
                                            .adminLevel(6)
                                            .build()
                                    ).firstOrNull()

                                when {
                                    newResult?.exception == null && !newResult?.data?.toList()
                                        .isNullOrEmpty() -> {
                                        filterRegionObjects(
                                            newResult?.data?.second ?: emptyList(),
                                            countryId,
                                            isoCode,
                                        )
                                    }

                                    newResult?.exception != null -> {
                                        emit(ApiResult.Failure(newResult.exception))
                                        return@flow
                                    }

                                    else -> {
                                        emit(ApiResult.Failure(exception = NoSuchElementException()))
                                        return@flow
                                    }
                                }
                            }
                    }

                    result.exception != null -> {
                        emit(ApiResult.Failure(exception = result.exception))
                        return@flow
                    }

                    else -> {
                        emit(ApiResult.Failure(exception = NoSuchElementException()))
                        return@flow
                    }
                }
            }

            filteredList.let {
                regionRepository.apply {
                    cacheRegions(it)
                    // we need region id from database, so we cannot emit result from API directly.
                    getRegions(countryId).firstOrNull()?.regionModels?.let {
                        emit(ApiResult.Success(data = it))
                    }
                }
            }
        }
    }
        .flowOn(coroutineDispatcher)
}
