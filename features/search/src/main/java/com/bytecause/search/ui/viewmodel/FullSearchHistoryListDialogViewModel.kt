package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchManager
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.search.ui.model.SearchHistoryParentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    fun loadSearchHistory(cache: Flow<RecentlySearchedPlaceList?>, stringArray: Array<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            cache.firstOrNull().let { entity ->
                entity ?: return@launch

                entity.placeList.sortedByDescending { it.timeStamp }
                    .groupBy { getParentTitle(it.timeStamp, stringArray) }
                    .map { (title, childList) ->
                        listOf(SearchHistoryParentItem(title, childList))
                    }.flatten().let {
                        _parentList.value = it
                    }
            }
        }
    }

    fun updateRecentlySearchedPlaces(
        element: RecentlySearchedPlace
    ) = flow {
        element.let {
            getRecentlySearchedPlaceList.firstOrNull()
                .let { savedPlaces ->
                    savedPlaces ?: return@flow
                    val updatedList =
                        (savedPlaces.placeList.filter { place -> place.placeId != it.placeId } + it)
                    emit(updatedList)
                }
        }
    }.flowOn(Dispatchers.IO)

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

    // DataStore operations.
    val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> =
        historyRepository.getRecentlySearchedPlaces()

    fun updateRecentlySearchedPlaces(entityList: List<RecentlySearchedPlace>) {
        viewModelScope.launch {
            historyRepository.updateRecentlySearchedPlaces(entityList)
        }
    }

    // AppSearch API operations.
    suspend fun searchCachedResult(query: String): List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity> =
        searchManager.searchCachedResult(query)

    override fun onCleared() {
        searchManager.closeSession()
        super.onCleared()
    }
}