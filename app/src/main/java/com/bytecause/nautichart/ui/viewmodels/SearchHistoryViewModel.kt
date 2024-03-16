package com.bytecause.nautichart.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.nautichart.data.local.SearchManager
import com.bytecause.nautichart.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.nautichart.data.repository.SearchHistoryDataStoreRepository
import com.bytecause.nautichart.domain.model.SearchedPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchHistoryViewModel @Inject constructor(
    private val historyRepository: SearchHistoryDataStoreRepository,
    private val searchManager: SearchManager
) : ViewModel() {

    private val _historyList = MutableStateFlow(listOf<SearchedPlace>())
    val historyList: StateFlow<List<SearchedPlace>> = _historyList.asStateFlow()

    val getRecentlySearchedPlaceList: Flow<RecentlySearchedPlaceList?> =
        historyRepository.getRecentlySearchedPlaces()

    // History DataStore operations.
    override fun onCleared() {
        searchManager.closeSession()
        super.onCleared()
    }

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

    var dataStoreSize: Int = 0
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val initList = mutableListOf<SearchedPlace>()

            getRecentlySearchedPlaceList.firstOrNull()?.placeList?.sortedByDescending { element -> element.timeStamp }
                ?.map { place ->
                    SearchedPlace(
                        placeId = place.placeId,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        addressType = place.type,
                        name = place.name,
                        displayName = place.displayName
                    )
                }?.let {
                    dataStoreSize = it.size
                    it.forEach {
                        if (initList.size < 7) {
                            initList.add(it)
                        }
                    }
                    _historyList.value = _historyList.value + initList
                }
        }
    }


    /*val combinedFlow: Flow<List<SearchedPlace>> = combine(
        getRecentlySearchedPlaceList,
        historyList
    )
    { datastoreList, _ ->

        Log.d("okay55", "on dd")

        val initList = mutableListOf<SearchedPlace>()

        // Add elements from the DataStore list
        datastoreList?.placeList?.sortedByDescending { element -> element.timeStamp }
            ?.map { place ->
                SearchedPlace(
                    placeId = place.placeId,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    addressType = place.type,
                    name = place.name,
                    displayName = place.displayName
                )
            }?.let {
                dataStoreSize = it.size
                it.forEach {
                    if (initList.size < 7) {
                        initList.add(it)
                    }
                }
            }
        _historyList.value = initList
        // Return the updated historyList
        _historyList.value
    }*/

    /*val combinedFlow: Flow<List<SearchedPlace>> = combine(
        getRecentlySearchedPlaceList,
        historyList
    )
    { datastoreList, historyList ->

        if (!datastoreList?.placeList.isNullOrEmpty()) {
            val initList = mutableListOf<SearchedPlace>()
            // Add elements from the DataStore list
            datastoreList?.placeList?.sortedByDescending { element -> element.timeStamp }
                ?.map { place ->
                    SearchedPlace(
                        placeId = place.placeId,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        addressType = place.type,
                        name = place.name,
                        displayName = place.displayName
                    )
                }?.let {
                    dataStoreSize = it.size
                    Log.d(TAG(this), it.joinToString())
                    initList.addAll(it)
                    _historyList.value = _historyList.value + initList
                }
            historyList
        } else historyList
    }*/

    /*private val _historyList = MutableLiveData<List<SearchedPlace>>()
    val historyList: LiveData<List<SearchedPlace>> = _historyList*/

    fun addElement(element: SearchedPlace) {
        Log.d("idk", "add element")
        _historyList.value = _historyList.value + element
    }

    fun clearElements() {
        _historyList.value = listOf()
    }

    fun addElements(elements: List<SearchedPlace>) {
        Log.d("idk", "add element")
        _historyList.value = _historyList.value + elements
    }

    fun removePosition(removeAt: Int) {
        val tempList = historyList.value.toMutableList()
        Log.d("idk123", tempList[removeAt].name)
        Log.d("idk123", removeAt.toString())
        Log.d("idk123", tempList.joinToString())
        //Log.d("idk123", totalCount.toString())
        // Log.d("idk123", historyList.value.size.toString())
        tempList.removeAt(removeAt)
        Log.d("idk123", tempList.joinToString())
        _historyList.value = tempList
    }

    fun updateRecentlySearchedPlaces(
        element: RecentlySearchedPlace,
        cache: Flow<RecentlySearchedPlaceList?>
    ) = flow {
        element.let {
            cache.firstOrNull()
                .let { savedPlaces ->
                    savedPlaces ?: return@flow
                    val updatedList =
                        (savedPlaces.placeList.filter { place -> place.placeId != it.placeId } + it)
                    emit(updatedList)
                }
        }
    }

    // AppSearch Api operations.
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
}