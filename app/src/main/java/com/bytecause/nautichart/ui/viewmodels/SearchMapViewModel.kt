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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class SearchMapViewModel @Inject constructor(
    private val searchMapRepository: SearchMapRepository,
    private val searchManager: SearchManager,
    private val historyRepository: SearchHistoryDataStoreRepository
) : ViewModel() {

    private var _uiSearchState = MutableStateFlow<UiState<SearchedPlace>?>(null)
    val uiSearchState get() = _uiSearchState.asStateFlow()

    suspend fun searchPlaces(query: String, cachedPlaces: List<SearchPlaceCacheEntity>? = null) {
        _uiSearchState.value = UiState(isLoading = true)
        if (cachedPlaces.isNullOrEmpty()) {
            when (val data = searchMapRepository.searchPlaces(query)) {
                is ApiResult.Success -> {
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
                items = cachedPlaces.map {
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

    fun cacheResult(result: List<SearchPlaceCacheEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            searchManager.putResults(result)
        }
    }

    suspend fun searchCachedResult(query: String): List<SearchPlaceCacheEntity> {
        return withContext(Dispatchers.IO) {
            searchManager.searchCachedResult(query)
        }
    }


    // History DataStore operations.
    override fun onCleared() {
        searchManager.closeSession()
        super.onCleared()
    }

    fun saveRecentlySearchedPlace(entity: RecentlySearchedPlace) {
        viewModelScope.launch {
            historyRepository.saveRecentlySearchedPlace(entity)
        }
    }

    val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> = historyRepository.getRecentlySearchedPlaces()


    fun deleteRecentlySearchedPlace(index: Int) {
        viewModelScope.launch {
            historyRepository.deleteRecentlySearchedPlace(index)
        }
    }

    fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>) {
        viewModelScope.launch {
            historyRepository.updateRecentlySearchedPlaces(entityList)
        }
    }
}