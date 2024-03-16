package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.remote.retrofit.NominatimRestApiService
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.NominatimApiModel
import com.bytecause.nautichart.domain.model.SearchedPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SearchMapRepository(private val nominatimRestApiService: NominatimRestApiService) {

    suspend fun searchPlaces(query: String): ApiResult<List<SearchedPlace>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = nominatimRestApiService.search(query)
                if (response.isSuccessful) {
                    ApiResult.Success(response.body()?.map { mapToSearchedPlace(it) }
                        ?: emptyList())
                } else ApiResult.Failure(exception = Exception("${response.code()}"))
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Failure(exception = e)
            }
        }
    }

    private fun mapToSearchedPlace(nominatimApiModel: NominatimApiModel): SearchedPlace {
        nominatimApiModel.let {
            return SearchedPlace(
                it.placeId,
                it.lat,
                it.lon,
                it.addressType,
                it.name,
                it.displayName,
                it.polygonCoordinates
            )
        }
    }


}