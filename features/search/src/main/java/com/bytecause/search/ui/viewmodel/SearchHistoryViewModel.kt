package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.search.data.local.appsearch.abstraction.SearchManager
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.search.mapper.asRecentlySearchedPlace
import com.bytecause.search.mapper.asRecentlySearchedPlaceUiModel
import com.bytecause.search.mapper.asSearchedPlaceUiModel
import com.bytecause.search.ui.model.RecentlySearchedPlaceUiModel
import com.bytecause.util.mappers.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchHistoryViewModel @Inject constructor(
    private val historyRepository: SearchHistoryRepository,
    private val searchManager: SearchManager
) : ViewModel() {

    /* Because historyList is limited to 7 elements and datastore is not,
      then we have to know the total count of elements in datastore during removing process. */
    var dataStoreSize: Int = -1
        private set

    val getRecentlySearchedPlaceList: Flow<List<SearchedPlaceUiModel>> =
        historyRepository.getRecentlySearchedPlaces()
            .map { originalList ->
                mapList(originalList.placeList) { it.asRecentlySearchedPlaceUiModel() }
                    .also { dataStoreSize = it.size }
                    .sortedByDescending { element -> element.timestamp }
                    .map { searchedPlace ->
                        SearchedPlaceUiModel(
                            placeId = searchedPlace.placeId,
                            latitude = searchedPlace.latitude,
                            longitude = searchedPlace.longitude,
                            addressType = searchedPlace.type,
                            name = searchedPlace.name,
                            displayName = searchedPlace.displayName
                        )
                    }
                    .take(8)
            }

    fun deleteRecentlySearchedPlace(index: Int) {
        viewModelScope.launch {
            historyRepository.deleteRecentlySearchedPlace(index)
        }
    }

    fun saveRecentlySearchedPlace(place: RecentlySearchedPlaceUiModel) {
        viewModelScope.launch {
            historyRepository.saveRecentlySearchedPlace(place.asRecentlySearchedPlace())
        }
    }

    fun searchCachedResult(query: String): Flow<List<SearchedPlaceUiModel>> =
        searchManager.searchCachedResult(query)
            .map { originalList -> mapList(originalList) { it.asSearchedPlaceUiModel() } }
}