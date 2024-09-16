package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.data.repository.abstractions.SearchManager
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.presentation.model.UiState
import com.bytecause.search.data.repository.abstractions.SearchMapRepository
import com.bytecause.search.mapper.asSearchedPlaceUiModel
import com.bytecause.util.mappers.mapList
import com.bytecause.util.mappers.mapNullInputList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

@HiltViewModel
class SearchMapViewModel
@Inject
constructor(
    private val searchMapRepositoryImpl: SearchMapRepository,
    private val searchManager: SearchManager,
    private val historyRepository: SearchHistoryRepository,
) : ViewModel() {
    var isLoading = false
        private set

    private var _uiSearchState =
        MutableStateFlow<UiState<SearchedPlaceUiModel>?>(null)
    val uiSearchState get() = _uiSearchState.asStateFlow()

    // place that should be shown on map
    private val _searchPlace = MutableSharedFlow<SearchedPlaceUiModel>(1)
    val searchPlace: SharedFlow<SearchedPlaceUiModel> = _searchPlace.asSharedFlow()

    // search place in cache database or make api call
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiSearchState.value = UiState(loading = Loading(true))
            isLoading = true
            val searchCache = searchCachedResult(query).firstOrNull()
            if (searchCache.isNullOrEmpty()) {
                when (val result = searchMapRepositoryImpl.searchPlaces(query).firstOrNull()) {
                    is ApiResult.Success -> {
                        val polylineAlgorithms =
                            com.bytecause.util.algorithms.PolylineAlgorithms()

                        result.data?.filter { searchedPlace ->
                            val searchedPlaceModelList =
                                searchCachedResult(
                                    searchedPlace.name.takeIf { it.isNotEmpty() }
                                        ?: searchedPlace.displayName,
                                )
                                    .firstOrNull()

                            searchedPlaceModelList?.any { element -> element.placeId == searchedPlace.placeId } == false
                        }?.map { searchedPlace ->

                            com.bytecause.data.local.room.tables.SearchPlaceCacheEntity(
                                nameSpace = "cached_place",
                                placeId = searchedPlace.placeId.toString(),
                                latitude = searchedPlace.latitude,
                                longitude = searchedPlace.longitude,
                                addressType = searchedPlace.addressType,
                                name = searchedPlace.name,
                                displayName = searchedPlace.displayName,
                                polygonCoordinates =
                                polylineAlgorithms.encode(
                                    com.bytecause.util.string.StringUtil.extractCoordinatesToGeoPointList(
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
                                ),
                                score = 1,
                            )
                        }?.let { mappedEntity ->
                            cacheResult(mappedEntity)
                            _uiSearchState.emit(
                                UiState(
                                    loading = Loading(false),
                                    items = mapNullInputList(result.data) { it.asSearchedPlaceUiModel() }
                                ),
                            )
                        }

                        _uiSearchState.emit(
                            UiState(
                                loading = Loading(false),
                                items = mapNullInputList(result.data) { it.asSearchedPlaceUiModel() }
                            )
                        )
                    }

                    is ApiResult.Failure -> {
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
            element.let { searchedPlace ->
                getRecentlySearchedPlaceList.firstOrNull().let { savedPlaces ->
                    savedPlaces ?: return@launch

                    val entity: RecentlySearchedPlace =
                        RecentlySearchedPlace.newBuilder().setPlaceId(searchedPlace.placeId)
                            .setLatitude(searchedPlace.latitude)
                            .setLongitude(searchedPlace.longitude)
                            .setName(searchedPlace.name)
                            .setDisplayName(searchedPlace.displayName)
                            .setType(searchedPlace.addressType)
                            .setTimeStamp(System.currentTimeMillis())
                            .build()

                    if (savedPlaces.placeList.any { it.placeId == entity.placeId }) {
                        val updatedList =
                            savedPlaces.placeList.filter { it.placeId != entity.placeId } + entity
                        updateRecentlySearchedPlaces(updatedList)
                    } else {
                        if (savedPlaces.placeList.size <= 50) {
                            saveRecentlySearchedPlace(
                                entity,
                            )
                        } else {
                            val updatedList =
                                savedPlaces.placeList.toMutableList().apply {
                                    removeAt(0)
                                    add(entity)
                                }
                            updateRecentlySearchedPlaces(updatedList)
                        }
                    }
                }

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
    fun initSession() {
        viewModelScope.launch {
            searchManager.openSession()
        }
    }

    // cache result returned from api
    private fun cacheResult(result: List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>) {
        viewModelScope.launch {
            searchManager.putResults(result)
        }
    }

    // search cached results in database
    private fun searchCachedResult(query: String): Flow<List<SearchedPlaceUiModel>> =
        searchManager.searchCachedResult(query)
            .map { originalList -> mapList(originalList) { it.asSearchedPlaceUiModel() } }

    // History DataStore operations.
    override fun onCleared() {
        searchManager.closeSession()
        super.onCleared()
    }

    private fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace) {
        viewModelScope.launch {
            historyRepository.saveRecentlySearchedPlace(entity)
        }
    }

    private val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> =
        historyRepository.getRecentlySearchedPlaces()

    private fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>) {
        viewModelScope.launch {
            historyRepository.updateRecentlySearchedPlaces(entityList)
        }
    }
}