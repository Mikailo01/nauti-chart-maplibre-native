package com.bytecause.nautichart.domain.usecase

import android.util.Log
import com.bytecause.nautichart.data.local.room.tables.Region
import com.bytecause.nautichart.data.repository.OverpassRepository
import com.bytecause.nautichart.data.repository.RegionRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.OverpassRelationModel
import com.bytecause.nautichart.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import javax.inject.Inject

class RegionUseCase @Inject constructor(
    private val regionRepository: RegionRepository,
    private val overpassRepository: OverpassRepository
) {

    // filters out all unwanted objects
    private fun filterRegionObjects(
        objects: List<OverpassRelationModel>,
        countryId: Int,
        isoCode: String
    ): List<Region> {
        val supportedBoundaryList = listOf("historic", "administrative")

        val filteredObjects = objects.filter {
            !it.tags.keys.contains("ISO3166-1")
                    && !it.tags.keys.contains("addr:city")
                    && it.tags["boundary"] in supportedBoundaryList
                    && if (it.tags["ISO3166-2"] == null) true else it.tags["ISO3166-2"]?.startsWith(
                isoCode
            ) == true
        }

        if (filteredObjects.isEmpty()) {
            return listOf(
                Region(
                    names = mapOf(
                        "name" to Locale(
                            Locale.getDefault().isO3Country,
                            isoCode
                        ).displayCountry
                    ),
                    countryId = countryId
                )
            )
        }

        return filteredObjects.map {
            Region(
                names = it.tags.filter { tag -> tag.key.startsWith("name") },
                countryId = countryId
            )
        }
    }

    fun getRegions(countryId: Int, isoCode: String, query: String) =
        flow {
            var filteredList: List<Region> = listOf()
            // if regions are cached in the database emit results and return flow, otherwise make query
            // to obtain them
            regionRepository.getRegions(countryId)
                .firstOrNull()?.regions?.takeIf { it.isNotEmpty() }?.let {
                    emit(ApiResult.Success(data = it))
                    return@flow
                } ?: run {
                overpassRepository.makeQuery<OverpassRelationModel>(query).let {
                    if (it.exception == null && !it.data.isNullOrEmpty()) {
                        filteredList =
                            filterRegionObjects(
                                it.data,
                                countryId,
                                isoCode
                            ).takeIf { it.isNotEmpty() } ?: let {
                                // If list is empty, do another query with adminLevel 6 (original query use adminLevel 4)
                                val result =
                                    overpassRepository.makeQuery<OverpassRelationModel>(
                                        com.bytecause.nautichart.util.SimpleOverpassQueryBuilder(
                                            format = com.bytecause.nautichart.util.SimpleOverpassQueryBuilder.FormatTypes.JSON,
                                            timeoutInSeconds = 90,
                                            geocodeAreaISO = isoCode,
                                            type = "relation",
                                            adminLevel = 6
                                        ).getQuery()
                                    )

                                if (result.exception == null && !result.data.isNullOrEmpty()) {
                                    filteredList = filterRegionObjects(
                                        result.data,
                                        countryId,
                                        isoCode
                                    )

                                    filteredList
                                } else if (result.exception != null && result.data.isNullOrEmpty()) {
                                    emit(ApiResult.Failure(exception = result.exception))
                                    return@flow
                                } else throw NoSuchElementException()
                            }
                    } else if (it.exception != null) {
                        emit(ApiResult.Failure(exception = it.exception))
                        return@flow
                    } else Log.d(TAG(this@RegionUseCase), "$isoCode empty")
                }
            }
            filteredList.let {
                regionRepository.apply {
                    cacheRegions(it)
                    // we need region id from database, so we cannot emit result from API directly.
                    getRegions(countryId).firstOrNull()?.regions?.let {
                        emit(ApiResult.Success(data = it))
                    }
                }
            }
        }
            .flowOn(Dispatchers.IO)

}