package com.bytecause.search.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.search.data.local.appsearch.abstraction.SearchManager
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.SearchedPlaceModel
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.presentation.model.UiState
import com.bytecause.search.data.local.appsearch.RECENTLY_SEARCHED_PLACE_NAMESPACE
import com.bytecause.search.data.local.appsearch.SearchPlaceCacheEntity
import com.bytecause.search.data.repository.abstractions.SearchMapRepository
import com.bytecause.search.mapper.asRecentlySearchedPlace
import com.bytecause.search.mapper.asRecentlySearchedPlaceUiModel
import com.bytecause.search.mapper.asSearchedPlaceUiModel
import com.bytecause.search.ui.model.RecentlySearchedPlaceUiModel
import com.bytecause.util.mappers.mapList
import com.bytecause.util.mappers.mapNullInputList
import com.bytecause.util.string.StringUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

@HiltViewModel
class SearchMapViewModel
@Inject
constructor(
    private val searchMapRepository: SearchMapRepository,
    private val searchManager: SearchManager,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    var isLoading = false
        private set

    private var _uiSearchState =
        MutableStateFlow<UiState<SearchedPlaceUiModel>?>(null)
    val uiSearchState get() = _uiSearchState.asStateFlow()

    // place that should be shown on map
    private val _searchPlace = MutableSharedFlow<SearchedPlaceUiModel>(1)
    val searchPlace: SharedFlow<SearchedPlaceUiModel> = _searchPlace.asSharedFlow()

    init {
        initSession()
    }

    // search place in cache database or make api call
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiSearchState.value = UiState(loading = Loading(true))
            isLoading = true

            val searchCache = searchCachedResult(query).firstOrNull()
            if (searchCache.isNullOrEmpty()) {
                when (val result = searchMapRepository.searchPlaces(query).firstOrNull()) {
                    is ApiResult.Success -> {

                        /* result.data?.filter { searchedPlace ->
                             val searchedPlaceModelList =
                                 searchCachedResult(
                                     searchedPlace.name.takeIf { it.isNotEmpty() }
                                         ?: searchedPlace.displayName,
                                 )
                                     .firstOrNull()

                             searchedPlaceModelList?.any { element -> element.placeId == searchedPlace.placeId } == false
                         }?.let { searchedPlaces ->



                             _uiSearchState.emit(
                                 UiState(
                                     loading = Loading(false),
                                     items = mapNullInputList(result.data) { it.asSearchedPlaceUiModel() }
                                 ),
                             )
                         }*/

                        result.data?.let {
                            cacheResult(it)
                        }

                        _uiSearchState.emit(
                            UiState(
                                loading = Loading(false),
                                items = mapNullInputList(result.data) { it.asSearchedPlaceUiModel() }
                            )
                        )
                    }

                    is ApiResult.Failure -> {

                        Log.d("idk", result.exception?.message.toString())
                        _uiSearchState.emit(UiState(error = result.exception))
                    }

                    else -> _uiSearchState.emit(UiState(loading = Loading(false)))
                }
            } else {
                _uiSearchState.emit(
                    UiState(
                        loading = Loading(false),
                        items = searchCache
                    )
                )
            }
            isLoading = false
        }
    }

    // save selected place to the proto datastore.
    fun saveAndShowSearchedPlace(element: SearchedPlaceUiModel) {
        viewModelScope.launch {
            element.asRecentlySearchedPlaceUiModel().let { searchedPlace ->

                saveRecentlySearchedPlace(searchedPlace)

                searchCachedResult(
                    searchedPlace.name.takeIf { it.isNotEmpty() }
                        ?: searchedPlace.displayName
                )
                    .firstOrNull()
                    ?.first {
                        it.placeId == searchedPlace.placeId
                    }?.let {
                        _searchPlace.emit(it)
                    }
            }
        }
    }

    fun sortListByDistance(
        list: List<SearchedPlaceUiModel>,
        position: LatLng?,
    ): List<SearchedPlaceUiModel> {
        position ?: return list

        return list.sortedBy {
            LatLng(
                it.latitude,
                it.longitude,
            ).distanceTo(position)
        }
    }

    // AppSearch API operations.
    private fun initSession() {
        viewModelScope.launch {
            searchManager.openSession()
        }
    }

    // cache result returned from api
    private suspend fun cacheResult(result: List<SearchedPlaceModel>) {
        val polylineAlgorithms =
            com.bytecause.util.algorithms.PolylineAlgorithms()

        result.map { searchedPlace ->
            SearchPlaceCacheEntity(
                nameSpace = RECENTLY_SEARCHED_PLACE_NAMESPACE,
                placeId = searchedPlace.placeId.toString(),
                latitude = searchedPlace.latitude,
                longitude = searchedPlace.longitude,
                addressType = searchedPlace.addressType,
                name = searchedPlace.name,
                displayName = searchedPlace.displayName,
                polygonCoordinates =
                withContext(Dispatchers.Default) {
                    polylineAlgorithms.encode(
                        StringUtil.extractCoordinatesToGeoPointList(
                            searchedPlace.polygonCoordinates
                        )
                            .let { latLngList ->
                                if (latLngList.size > 1000) {
                                    polylineAlgorithms.simplifyPolyline(
                                        latLngList,
                                        0.001,
                                    )
                                } else {
                                    latLngList
                                }
                            },
                    )
                },
                score = 1
            )
        }.let {
            searchManager.putResults(it)
        }
    }

    // search cached results in database
    private fun searchCachedResult(query: String): Flow<List<SearchedPlaceUiModel>> =
        searchManager.searchCachedResult(query)
            .map { originalList -> mapList(originalList) { it.asSearchedPlaceUiModel() } }

    override fun onCleared() {
        super.onCleared()
        searchManager.closeSession()
    }

    private fun saveRecentlySearchedPlace(entity: RecentlySearchedPlaceUiModel) {
        viewModelScope.launch {
            searchHistoryRepository.saveRecentlySearchedPlace(entity.asRecentlySearchedPlace())
        }
    }
}