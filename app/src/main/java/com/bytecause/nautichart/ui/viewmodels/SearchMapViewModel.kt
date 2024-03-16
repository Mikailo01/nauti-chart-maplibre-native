package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.nautichart.data.local.SearchManager
import com.bytecause.nautichart.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.nautichart.data.repository.SearchHistoryDataStoreRepository
import com.bytecause.nautichart.data.repository.SearchMapRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.SearchedPlace
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.util.PolylineAlgorithms
import com.bytecause.nautichart.util.StringUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class SearchMapViewModel @Inject constructor(
    private val searchMapRepository: SearchMapRepository,
    private val searchManager: SearchManager,
    private val historyRepository: SearchHistoryDataStoreRepository
) : ViewModel() {

    var isLoading = false
        private set

    private var _uiSearchState = MutableStateFlow<UiState<SearchedPlace>?>(null)
    val uiSearchState get() = _uiSearchState.asStateFlow()

    // place that should be shown on map
    private val _searchPlace = MutableSharedFlow<SearchPlaceCacheEntity>(1)
    val searchPlace: SharedFlow<SearchPlaceCacheEntity> = _searchPlace.asSharedFlow()

    // search place in cache database or make api call
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiSearchState.value = UiState(isLoading = true)
            isLoading = true
            val searchCache = searchCachedResult(query)
            if (searchCache.isEmpty()) {
                when (val data = searchMapRepository.searchPlaces(query)) {
                    is ApiResult.Success -> {

                        val polylineAlgorithms = PolylineAlgorithms()
                        data.data?.filterNot { searchedPlace ->
                            val searchedPlaceList =
                                searchCachedResult(searchedPlace.name.takeIf { it.isNotEmpty() }
                                    ?: searchedPlace.displayName).map { cachedEntity ->
                                    // Map only necessary properties.
                                    SearchedPlace(
                                        placeId = cachedEntity.placeId.toLong(),
                                        latitude = cachedEntity.latitude,
                                        longitude = cachedEntity.longitude
                                    )
                                }

                            searchedPlaceList.any { element -> element.placeId == searchedPlace.placeId }
                        }?.map { searchedPlace ->

                            SearchPlaceCacheEntity(
                                nameSpace = "cached_place",
                                placeId = searchedPlace.placeId.toString(),
                                latitude = searchedPlace.latitude,
                                longitude = searchedPlace.longitude,
                                addressType = searchedPlace.addressType,
                                name = searchedPlace.name,
                                displayName = searchedPlace.displayName,
                                polygonCoordinates = polylineAlgorithms.encode(
                                    StringUtil.extractCoordinatesToGeoPointList(searchedPlace.polygonCoordinates)
                                        .let { geoPointList ->
                                            if (geoPointList.size > 1000) polylineAlgorithms.simplifyPolyline(
                                                geoPointList,
                                                0.001
                                            )
                                            else geoPointList
                                        }
                                ),
                                score = 1
                            )
                        }?.let { mappedEntity ->
                            cacheResult(mappedEntity)
                            _uiSearchState.emit(
                                UiState(
                                    isLoading = false,
                                    items = data.data
                                )
                            )
                        }

                        _uiSearchState.emit(
                            UiState(
                                isLoading = false,
                                items = data.data ?: emptyList()
                            )
                        )
                    }

                    is ApiResult.Failure -> {
                        when (data.exception) {
                            is ConnectException -> {
                                _uiSearchState.emit(UiState(error = UiState.Error.ServiceUnavailable))
                            }

                            else -> _uiSearchState.emit(UiState(error = UiState.Error.NetworkError))
                        }
                    }
                }
            } else _uiSearchState.emit(
                UiState(
                    isLoading = false,
                    items = searchCache.map {
                        SearchedPlace(
                            it.placeId.toLong(),
                            it.latitude,
                            it.longitude,
                            it.addressType,
                            it.name,
                            it.displayName
                        )
                    }
                )
            )
            _uiSearchState.value = _uiSearchState.value?.copy(isLoading = false)
            isLoading = false
        }
    }

    // save selected place to the proto datastore.
    fun saveAndShowSearchedPlace(element: SearchedPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            element.let { searchedPlace ->
                getRecentlySearchedPlaceList.firstOrNull().let { savedPlaces ->
                    savedPlaces ?: return@launch
                    /* if (searchedPlace.placeId == 0L) {
                         mapSharedViewModel.setPlaceToFind(
                             SearchPlaceCacheEntities(
                                 latitude = searchedPlace.latitude,
                                 longitude = searchedPlace.longitude
                             )
                         )
                         withContext(Dispatchers.Main) {
                             findNavController().popBackStack(R.id.map_dest, false)
                         }
                         return@launch
                     }*/

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
                        if (savedPlaces.placeList.size <= 50) saveRecentlySearchedPlace(
                            entity
                        )
                        else {
                            val updatedList = savedPlaces.placeList.toMutableList().apply {
                                removeAt(0)
                                add(entity)
                            }
                            updateRecentlySearchedPlaces(updatedList)
                        }
                    }
                }

                searchCachedResult(searchedPlace.name.takeIf { it.isNotEmpty() }
                    ?: searchedPlace.displayName).first {
                    it.placeId.toLong() == searchedPlace.placeId
                }.let {
                    _searchPlace.emit(it)
                }
            }
        }
    }

    fun sortListByDistance(
        list: List<SearchedPlace>,
        position: GeoPoint?
    ): List<SearchedPlace> {
        position ?: return list

        return list.sortedBy {
            GeoPoint(
                it.latitude,
                it.longitude
            ).distanceToAsDouble(position)
        }
    }

    // AppSearch API operations.
    fun initSession() {
        viewModelScope.launch(Dispatchers.IO) {
            searchManager.openSession()
        }
    }


    // cache result returned from api
    private fun cacheResult(result: List<SearchPlaceCacheEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            searchManager.putResults(result)
        }
    }

    // search cached results in database
    private suspend fun searchCachedResult(query: String): List<SearchPlaceCacheEntity> =
        searchManager.searchCachedResult(query)


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

