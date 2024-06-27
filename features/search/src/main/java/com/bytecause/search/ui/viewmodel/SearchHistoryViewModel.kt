package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.search.data.local.appsearch.SearchManager
import com.bytecause.search.data.repository.abstractions.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchHistoryViewModel @Inject constructor(
    private val historyRepository: SearchHistoryRepository,
    private val searchManager: SearchManager
) : ViewModel() {

    val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> =
        historyRepository.getRecentlySearchedPlaces()

    // History DataStore operations.
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

    // AppSearch Api operations.
    suspend fun searchCachedResult(query: String): List<SearchPlaceCacheEntity> {
        return withContext(Dispatchers.IO) {
            searchManager.searchCachedResult(query)
        }
    }
}