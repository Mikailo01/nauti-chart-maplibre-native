package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.data.repository.abstractions.SearchManager
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.search.mapper.asSearchedPlaceUiModel
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

    val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> =
        historyRepository.getRecentlySearchedPlaces()

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

    fun searchCachedResult(query: String): Flow<List<SearchedPlaceUiModel>> =
        searchManager.searchCachedResult(query)
            .map { originalList -> mapList(originalList) { it.asSearchedPlaceUiModel() } }
}