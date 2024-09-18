package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.data.repository.abstractions.SearchManager
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.search.mapper.asRecentlySearchedPlace
import com.bytecause.search.mapper.asRecentlySearchedPlaceUiModel
import com.bytecause.search.mapper.asSearchedPlaceUiModel
import com.bytecause.search.ui.model.RecentlySearchedPlaceUiModel
import com.bytecause.search.ui.model.SearchHistoryParentItem
import com.bytecause.util.mappers.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.sql.Date
import java.sql.Timestamp
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class FullSearchHistoryListDialogViewModel @Inject constructor(
    private val historyRepository: SearchHistoryRepository,
    private val searchManager: SearchManager
) : ViewModel() {

    private val _parentList = MutableStateFlow(listOf<SearchHistoryParentItem>())
    val parentList: StateFlow<List<SearchHistoryParentItem>> = _parentList.asStateFlow()

    fun loadSearchHistory(stringArray: Array<String>) {
        viewModelScope.launch {
            getRecentlySearchedPlaceList.firstOrNull()?.let { searchedPlaces ->
                searchedPlaces.sortedByDescending { it.timestamp }
                    .groupBy { getParentTitle(it.timestamp, stringArray) }
                    .map { (title, childList) ->
                        SearchHistoryParentItem(title, childList)
                    }
                    .also {
                        _parentList.value = it
                    }
            }
        }
    }

    suspend fun saveRecentlySearchedPlace(element: RecentlySearchedPlaceUiModel) {
        historyRepository.saveRecentlySearchedPlace(element.asRecentlySearchedPlace())
    }

    private fun getParentTitle(timestamp: Long, stringArray: Array<String>): String {
        val currentTimestamp = Timestamp(System.currentTimeMillis())
        val currentDate = Date(currentTimestamp.time)

        val elementTimestamp = Timestamp(timestamp)
        val elementDate = Date(elementTimestamp.time)

        val currentCalendar = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val elementCalendar = Calendar.getInstance().apply {
            time = elementDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val differenceInMillis = abs(currentCalendar.timeInMillis - elementCalendar.timeInMillis)
        val differenceInDays = (differenceInMillis / (24 * 60 * 60 * 1000)).toInt()

        return when {
            differenceInDays == 0 -> stringArray[0]
            differenceInDays == 1 -> stringArray[1]
            differenceInDays <= 7 -> stringArray[2]
            else -> stringArray[3]
        }
    }

    private val getRecentlySearchedPlaceList: Flow<List<RecentlySearchedPlaceUiModel>> =
        historyRepository.getRecentlySearchedPlaces()
            .map { originalList -> mapList(originalList.placeList) { it.asRecentlySearchedPlaceUiModel() } }

    fun searchCachedResult(query: String): Flow<List<SearchedPlaceUiModel>> =
        searchManager.searchCachedResult(query)
            .map { originalList -> mapList(originalList) { it.asSearchedPlaceUiModel() } }

    override fun onCleared() {
        super.onCleared()
        searchManager.closeSession()
    }
}