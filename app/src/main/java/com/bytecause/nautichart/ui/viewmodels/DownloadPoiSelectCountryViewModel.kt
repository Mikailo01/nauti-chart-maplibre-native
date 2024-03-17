package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.local.room.tables.Country
import com.bytecause.nautichart.data.local.room.tables.Region
import com.bytecause.nautichart.data.repository.ContinentDatabaseRepository
import com.bytecause.nautichart.data.repository.CountryDataExtractSizeRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.CountryParentItem
import com.bytecause.nautichart.domain.model.RegionChildItem
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.domain.usecase.PoiUseCase
import com.bytecause.nautichart.domain.usecase.RegionUseCase
import com.bytecause.nautichart.ui.view.fragment.getKeyByIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.util.Locale
import javax.inject.Inject

private const val MAX_REGIONS_TO_DOWNLOAD = 2

@HiltViewModel
class DownloadPoiSelectCountryViewModel @Inject constructor(
    private val continentDatabaseRepository: ContinentDatabaseRepository,
    private val countryDataExtractSizeRepository: CountryDataExtractSizeRepository,
    private val regionUseCase: RegionUseCase,
    private val poiUseCase: PoiUseCase
) : ViewModel() {

    private val _mapContent = MutableStateFlow(mapOf<String, CountryParentItem>())
    val mapContent: StateFlow<Map<String, CountryParentItem>> = _mapContent.asStateFlow()

    private val _downloadQueueMap = mutableMapOf<Int, MutableList<Int>>()
    val downloadQueueMap get() = _downloadQueueMap.toMap()

    private val _downloadButtonVisibility = MutableStateFlow(false)
    val downloadButtonVisibility: StateFlow<Boolean> get() = _downloadButtonVisibility.asStateFlow()

    private val _countryList = mutableListOf<Country>()
    val countryList get() = _countryList.toList()

    private val _regionUiStateLiveData = MutableLiveData<UiState<Region>>(UiState())
    val regionUiStateLiveData: LiveData<UiState<Region>> get() = _regionUiStateLiveData

    private val _poiDownloadUiStateLiveData = MutableLiveData<UiState<String>>(UiState())
    val poiDownloadUiStateLiveData: LiveData<UiState<String>> get() = _poiDownloadUiStateLiveData

    private var downloadJob: Job? = null

    var recyclerViewExpandedStateList = listOf<Boolean>()
        private set

    fun saveRecyclerViewExpandedStates(stateList: List<Boolean>) {
        recyclerViewExpandedStateList = stateList
    }

    fun cancelDownloadJob() {
        downloadJob?.cancel()
        downloadJob = null
        showDownloadProgressBar(false)
    }

    fun getRegions(countryId: Int, isoCode: String, query: String) {
        viewModelScope.launch {
            _regionUiStateLiveData.postValue(UiState(isLoading = true))
            when (val data = regionUseCase.getRegions(countryId, isoCode, query).firstOrNull()) {
                is ApiResult.Success -> {
                    _regionUiStateLiveData.postValue(UiState(
                        isLoading = false,
                        items = data.data?.sortedBy {
                            it.names["name:${Locale.getDefault().language}"] ?: it.names["name"]
                        } ?: listOf()
                    ))
                }

                is ApiResult.Failure -> {
                    when (data.exception) {
                        is ConnectException -> {
                            _regionUiStateLiveData.postValue(
                                UiState(
                                    isLoading = false,
                                    error = UiState.Error.ServiceUnavailable
                                )
                            )
                        }

                        else -> {
                            _regionUiStateLiveData.postValue(
                                UiState(
                                    isLoading = false,
                                    error = UiState.Error.NetworkError
                                )
                            )
                        }
                    }
                }

                else -> {

                }
            }
        }
    }

    fun getPois(regionName: String, query: String) {
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            _poiDownloadUiStateLiveData.postValue(UiState(isLoading = true))
            when (val data = poiUseCase.getPoiResultByRegion(regionName, query).firstOrNull()) {
                is ApiResult.Success -> {
                    _poiDownloadUiStateLiveData.postValue(
                        UiState(
                            isLoading = false,
                            items = listOf(data.data ?: "")
                        )
                    )
                }

                is ApiResult.Failure -> {
                    when (data.exception) {
                        is ConnectException -> {
                            _poiDownloadUiStateLiveData.postValue(
                                UiState(
                                    isLoading = false,
                                    error = UiState.Error.ServiceUnavailable
                                )
                            )
                        }

                        else -> {
                            _poiDownloadUiStateLiveData.postValue(
                                UiState(
                                    isLoading = false,
                                    error = UiState.Error.NetworkError
                                )
                            )
                        }
                    }
                }

                else -> {

                }
            }
        }
    }

    fun addToDownloadQueue(countryPosition: Int, regionPosition: Int) {
        _downloadQueueMap[countryPosition]?.add(regionPosition) ?: run {
            _downloadQueueMap[countryPosition] = mutableListOf(regionPosition)
        }
        _downloadButtonVisibility.value = true
    }

    private fun toggleCheckBoxEnabled() {
        val updatedContent = mapContent.value.mapValues {
            val updatedRegionList = it.value.regionList.map { regionChild ->
                regionChild.copy(isCheckBoxEnabled = !(!regionChild.isChecked && getDownloadQueueMapSize() >= MAX_REGIONS_TO_DOWNLOAD))
            }
            CountryParentItem(
                regionList = updatedRegionList,
                size = it.value.size,
                isLoading = it.value.isLoading
            )
        }
        _mapContent.value = updatedContent
    }

    private fun getDownloadQueueMapSize(): Int {
        var totalValues = 0
        for (valuesList in downloadQueueMap.values) {
            totalValues += valuesList.size
        }
        return totalValues
    }

    fun removeFromDownloadQueue(countryPosition: Int, regionPosition: Int) {
        _downloadQueueMap[countryPosition]?.let { regionList ->
            regionList.remove(regionPosition)

            // Remove the country entry if the region list becomes empty
            if (regionList.isEmpty()) {
                _downloadQueueMap.remove(countryPosition)
                if (downloadQueueMap.values.all { it.isEmpty() }) {
                    _downloadButtonVisibility.value = false
                }
            }
        }
    }

    private fun resetDownloadQueue() {
        _downloadQueueMap.clear()
        _downloadButtonVisibility.value = false
    }

    fun isDownloading(): Boolean {
        return mapContent.value.any { it.value.regionList.any { region -> region.isDownloading } }
    }

    private fun updateMapElement(key: String, item: CountryParentItem) {
        val map = mutableMapOf<String, CountryParentItem>()

        map += _mapContent.value
        map[key] = item

        _mapContent.value = map
    }

    fun changeRegionLoadingState(position: Int) {
        mapContent.value[mapContent.value.getKeyByIndex(position)]?.let { field ->
            updateMapElement(
                mapContent.value.getKeyByIndex(position),
                field.copy(isLoading = true)
            )
        }
    }

    fun updateRegionClickedState(parentPosition: Int, childPosition: Int) {
        val regionList =
            mapContent.value[mapContent.value.getKeyByIndex(parentPosition)]?.regionList?.mapIndexed { index, regionChildItem ->
                if (index == childPosition) {
                    RegionChildItem(
                        region = regionChildItem.region,
                        isChecked = !regionChildItem.isChecked,
                        isDownloading = false,
                        isCheckBoxEnabled = true,
                        size = regionChildItem.size
                    )
                } else {
                    RegionChildItem(
                        region = regionChildItem.region,
                        isChecked = regionChildItem.isChecked,
                        isDownloading = false,
                        isCheckBoxEnabled = regionChildItem.isCheckBoxEnabled,//!(!regionChildItem.isChecked && getDownloadQueueMapSize() >= MAX_REGIONS_TO_DOWNLOAD),
                        size = regionChildItem.size
                    )
                }
            }
        if (!regionList.isNullOrEmpty()) {
            mapContent.value[mapContent.value.getKeyByIndex(parentPosition)]?.let {
                updateMapElement(
                    mapContent.value.getKeyByIndex(parentPosition),
                    it.copy(regionList = regionList, isLoading = false)
                )
                toggleCheckBoxEnabled()
            }
        }
    }

    fun resetCheckedState() {
        val queue = downloadQueueMap
        for ((countryPosition, regionPosition) in queue) {
            for (position in regionPosition) {
                updateRegionClickedState(countryPosition, position)
            }
        }
        resetDownloadQueue()
    }

    fun cancelRegionsLoading() {
        for ((key, value) in mapContent.value) {
            if (value.isLoading) {
                mapContent.value[key]?.let {
                    updateMapElement(key, item = it.copy(isLoading = false))
                }
            }
        }
    }

    fun showCountryRegions(items: List<Region>, continentName: String) {
        countryList.find {
            items.any { region -> region.countryId == it.id }
        }?.let { country ->

            val countryName = Locale(
                Locale.getDefault().isO3Country,
                country.iso2
            ).displayCountry

            if (mapContent.value[countryName]?.regionList?.isEmpty() == false) return@let

            updateMapElement(
                key = countryName,
                item = CountryParentItem(
                    regionList = items.map { region ->
                        RegionChildItem(
                            region = region,
                            isChecked = false,
                            isDownloading = false,
                            isCheckBoxEnabled = getDownloadQueueMapSize() < MAX_REGIONS_TO_DOWNLOAD,
                            size = ""
                        )
                    },
                    size = mapContent.value[countryName]?.copy()?.size
                        ?: "",
                    isLoading = false
                )
            )

            // Fetch size of each region from Geofabrik website and update
            // size field of each region element if present.
            mapContent.value[countryName]?.regionList?.let innerLet@{
                if (it.size <= 1) return@innerLet

                fetchRegionSize(
                    region = continentName,
                    country = countryName.lowercase()
                )
            }
        }
    }

    fun showDownloadProgressBar(showLoading: Boolean) {
        mapContent.value.forEach { mutableEntry ->
            val regionChildItemList = mutableEntry.value.regionList.map {
                if (it.isChecked) {
                    RegionChildItem(
                        region = it.region,
                        isChecked = true,
                        isDownloading = showLoading,
                        isCheckBoxEnabled = it.isCheckBoxEnabled,
                        size = it.size
                    )
                } else {
                    RegionChildItem(
                        region = it.region,
                        isChecked = false,
                        isDownloading = false,
                        isCheckBoxEnabled = it.isCheckBoxEnabled,
                        size = it.size
                    )
                }
            }

            updateMapElement(
                mutableEntry.key, CountryParentItem(
                    regionList = regionChildItemList,
                    size = mapContent.value[mutableEntry.key]?.copy()?.size ?: "",
                    isLoading = false
                )
            )
        }
    }

    fun getRegionNameFromQueue(): List<String> {
        val regionList = mutableListOf<String>()

        for ((key, value) in downloadQueueMap) {
            mapContent.value[mapContent.value.getKeyByIndex(key)]?.let { countryParentItem ->
                value.forEach { regionPosition ->
                    countryParentItem.regionList[regionPosition].let { regionEntity ->
                        regionList.add(regionEntity.region.names["name"] ?: "")
                    }
                }
            }
        }
        return regionList
    }

    private fun fetchRegionSize(region: String, country: String? = null) {
        viewModelScope.launch {
            countryDataExtractSizeRepository.fetchRegionSize(
                region.lowercase().replace("[&\\s]+".toRegex(), "-"),
                country
            ).let { map ->
                if (country == null) {
                    mapContent.value.keys.forEachIndexed { index, key ->
                        updateMapElement(
                            key = key,
                            item = CountryParentItem(
                                mapContent.value[key]?.regionList ?: listOf(),
                                size = map[countryList[index].iso2]
                                    ?: "",
                                isLoading = false
                            )
                        )
                    }
                } else {
                    mapContent.value.mapValues { (_, value) ->
                        value.copy(
                            regionList = value.regionList.map { regionChildItem ->
                                val key = map.keys.find { key ->
                                    regionChildItem.region.names.any {
                                        it.value.equals(
                                            key,
                                            true
                                        )
                                    }
                                }
                                RegionChildItem(
                                    region = regionChildItem.region,
                                    isChecked = regionChildItem.isChecked,
                                    isDownloading = regionChildItem.isDownloading,
                                    isCheckBoxEnabled = regionChildItem.isCheckBoxEnabled,
                                    size = map[key] ?: ""
                                )
                            }
                        )
                    }.let {
                        _mapContent.value = it
                    }
                }
            }
        }
    }

    fun getAssociatedCountries(continentId: Int, region: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (countryList.isNotEmpty()) return@launch
            continentDatabaseRepository.getAssociatedCountries(continentId).firstOrNull()?.let {

                _countryList.addAll(it.countries.sortedBy { country -> country.name })
                _countryList.forEach { country ->

                    // TODO("If no locale found ISO code is used as last-resort value, exclude all non-countries from static database.")
                    val mapKey = Locale(
                        Locale.getDefault().isO3Country,
                        country.iso2
                    ).displayCountry

                    updateMapElement(
                        key = mapKey,
                        item = CountryParentItem(
                            regionList = listOf(),
                            size = "",
                            isLoading = false
                        )
                    )
                }
                fetchRegionSize(region = region)
            }
        }
    }
}