package com.bytecause.pois.ui.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.CountryModel
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.RegionModel
import com.bytecause.domain.usecase.GetRegionsUseCase
import com.bytecause.pois.data.repository.abstractions.ContinentRepository
import com.bytecause.pois.data.repository.abstractions.CountryDataExtractSizeRepository
import com.bytecause.pois.ui.getKeyByIndex
import com.bytecause.pois.ui.model.CountryParentItem
import com.bytecause.pois.ui.model.RegionChildItem
import com.bytecause.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import java.net.ConnectException
import java.util.Locale
import javax.inject.Inject

// set to 1 to avoid big query results from the API
private const val MAX_REGIONS_TO_DOWNLOAD = 1

@HiltViewModel
class DownloadPoiSelectCountryViewModel
@Inject
constructor(
    private val continentRepository: ContinentRepository,
    private val countryDataExtractSizeRepository: CountryDataExtractSizeRepository,
    private val getRegionsUseCase: GetRegionsUseCase
) : ViewModel() {
    private val _mapContent = MutableStateFlow(mapOf<String, CountryParentItem>())
    val mapContent: StateFlow<Map<String, CountryParentItem>> = _mapContent.asStateFlow()

    private val _downloadQueueMap = mutableMapOf<Int, MutableList<Int>>()
    val downloadQueueMap get() = _downloadQueueMap.toMap()

    private val _downloadButtonVisibility = MutableStateFlow(false)
    val downloadButtonVisibility: StateFlow<Boolean> get() = _downloadButtonVisibility.asStateFlow()

    private val _countryEntityList = mutableListOf<CountryModel>()
    val countryEntityList get() = _countryEntityList.toList()

    private val _regionEntityUiStateLiveData = MutableLiveData<UiState<RegionModel>>(UiState())
    val regionEntityUiStateLiveData: LiveData<UiState<RegionModel>> get() = _regionEntityUiStateLiveData

    private val _poiDownloadUiState = MutableStateFlow<UiState<Nothing>?>(null)
    val poiDownloadUiState: StateFlow<UiState<Nothing>?> = _poiDownloadUiState.asStateFlow()

    var recyclerViewExpandedStateList = listOf<Boolean>()
        private set

    fun saveRecyclerViewExpandedStates(stateList: List<Boolean>) {
        recyclerViewExpandedStateList = stateList
    }

    fun cancelDownloadJob() {
        showDownloadProgressBar(false)
    }

    init {
        viewModelScope.launch {
            ServiceApiResultListener.eventFlow.collect { event ->
                when (event) {
                    is ServiceEvent.RegionPoiDownload -> {
                        when (val result = event.result) {
                            is ApiResult.Success -> {
                                updateRegionIsDownloaded()
                                resetCheckedState()

                                _poiDownloadUiState.emit(
                                    UiState(
                                        loading = Loading(false),
                                        items = emptyList(),
                                    )
                                )
                            }

                            is ApiResult.Failure -> {
                                _poiDownloadUiState.emit(
                                    UiState(
                                        loading = Loading(false),
                                        error = result.exception,
                                    )
                                )
                            }

                            is ApiResult.Progress -> {
                                result.progress?.let { progress ->
                                    _poiDownloadUiState.emit(
                                        UiState(
                                            loading = Loading(
                                                isLoading = true,
                                                progress = progress
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is ServiceEvent.RegionPoiDownloadStarted -> {
                        _poiDownloadUiState.emit(UiState(loading = Loading(true)))
                    }

                    is ServiceEvent.RegionPoiDownloadCancelled -> {
                        _poiDownloadUiState.emit(null)
                        cancelDownloadJob()
                    }

                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    fun getRegions(
        countryId: Int,
        isoCode: String,
        query: String,
    ) {
        viewModelScope.launch {
            _regionEntityUiStateLiveData.postValue(UiState(loading = Loading(true)))
            when (val data = getRegionsUseCase(countryId, isoCode, query).firstOrNull()) {
                is ApiResult.Success -> {
                    _regionEntityUiStateLiveData.postValue(
                        UiState(
                            loading = Loading(false),
                            items =
                            data.data?.sortedBy {
                                it.names["name:${Locale.getDefault().language}"]
                                    ?: it.names["name:en"]
                                    ?: it.names["name"]
                            } ?: emptyList(),
                        )
                    )
                }

                is ApiResult.Failure -> {
                    when (data.exception) {
                        is ConnectException -> {
                            _regionEntityUiStateLiveData.postValue(
                                UiState(
                                    loading = Loading(false),
                                    error = ConnectException(),
                                )
                            )
                        }

                        is NoSuchElementException -> {
                            _regionEntityUiStateLiveData.postValue(
                                UiState(
                                    loading = Loading(false),
                                    error = NoSuchElementException(),
                                )
                            )
                        }

                        else -> {
                            _regionEntityUiStateLiveData.postValue(
                                UiState(
                                    loading = Loading(false),
                                    error = IOException(),
                                )
                            )
                        }
                    }
                }

                else -> {
                    _regionEntityUiStateLiveData.postValue(
                        UiState(
                            loading = Loading(false),
                            items = emptyList(),
                        )
                    )
                }
            }
        }
    }

    fun addToDownloadQueue(
        countryPosition: Int,
        regionPosition: Int,
    ) {
        _downloadQueueMap[countryPosition]?.add(regionPosition) ?: run {
            _downloadQueueMap[countryPosition] = mutableListOf(regionPosition)
        }
        _downloadButtonVisibility.value = true
    }

    private fun toggleCheckBoxEnabled() {
        val updatedContent =
            mapContent.value.mapValues {
                val updatedRegionList =
                    it.value.regionList.map { regionChild ->
                        regionChild.copy(
                            isCheckBoxEnabled = !(!regionChild.isChecked && getDownloadQueueMapSize() >= MAX_REGIONS_TO_DOWNLOAD),
                        )
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

    fun removeFromDownloadQueue(
        countryPosition: Int,
        regionPosition: Int,
    ) {
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
        return mapContent.value.any { it.value.regionList.any { region -> region.loading.isLoading } }
    }

    private fun updateMapElement(
        key: String,
        item: CountryParentItem,
    ) {
        val map = mutableMapOf<String, CountryParentItem>()

        map += _mapContent.value
        map[key] = item

        _mapContent.update { map }
    }

    fun regionIsLoadingState(position: Int) {
        mapContent.value[mapContent.value.getKeyByIndex(position)]?.let { field ->
            updateMapElement(
                mapContent.value.getKeyByIndex(position),
                field.copy(isLoading = true)
            )
        }
    }

    fun cancelRegionsLoadingState() {
        for ((key, value) in mapContent.value) {
            if (value.isLoading) {
                mapContent.value[key]?.let {
                    updateMapElement(key, item = it.copy(isLoading = false))
                }
            }
        }
    }

    fun updateRegionClickedState(
        parentPosition: Int,
        childPosition: Int,
    ) {
        val regionList =
            mapContent.value[mapContent.value.getKeyByIndex(parentPosition)]?.regionList?.mapIndexed { index, regionChildItem ->
                if (index == childPosition) {
                    RegionChildItem(
                        regionEntity = regionChildItem.regionEntity,
                        isChecked = !regionChildItem.isChecked,
                        loading = Loading(false),
                        isCheckBoxEnabled = true,
                        size = regionChildItem.size,
                    )
                } else {
                    RegionChildItem(
                        regionEntity = regionChildItem.regionEntity,
                        isChecked = regionChildItem.isChecked,
                        loading = Loading(isLoading = false),
                        isCheckBoxEnabled = regionChildItem.isCheckBoxEnabled,
                        size = regionChildItem.size,
                    )
                }
            }
        if (!regionList.isNullOrEmpty()) {
            mapContent.value[mapContent.value.getKeyByIndex(parentPosition)]?.let {
                updateMapElement(
                    mapContent.value.getKeyByIndex(parentPosition),
                    it.copy(regionList = regionList, isLoading = false),
                )
                toggleCheckBoxEnabled()
            }
        }
    }

    private fun updateRegionIsDownloaded() {
        for ((countryPosition, regionPositions) in downloadQueueMap) {
            for (position in regionPositions) {
                val regionList =
                    mapContent.value[mapContent.value.getKeyByIndex(countryPosition)]?.regionList?.mapIndexed {
                            index,
                            regionChildItem,
                        ->
                        if (index == position) {
                            RegionChildItem(
                                regionEntity = regionChildItem.regionEntity.copy(isDownloaded = true),
                                isChecked = regionChildItem.isChecked,
                                loading = Loading(isLoading = false),
                                isCheckBoxEnabled = true,
                                size = regionChildItem.size,
                            )
                        } else {
                            RegionChildItem(
                                regionEntity = regionChildItem.regionEntity,
                                isChecked = regionChildItem.isChecked,
                                loading = Loading(isLoading = false),
                                isCheckBoxEnabled = regionChildItem.isCheckBoxEnabled,
                                size = regionChildItem.size,
                            )
                        }
                    }

                if (!regionList.isNullOrEmpty()) {
                    mapContent.value[mapContent.value.getKeyByIndex(countryPosition)]?.let {
                        updateMapElement(
                            mapContent.value.getKeyByIndex(countryPosition),
                            it.copy(regionList = regionList, isLoading = false),
                        )
                    }
                }
            }
        }
    }

    private fun resetCheckedState() {
        for ((countryPosition, regionPosition) in downloadQueueMap) {
            for (position in regionPosition) {
                updateRegionClickedState(countryPosition, position)
            }
        }
        resetDownloadQueue()
    }

    private fun getCountryName(iso2: String): String = Locale(
        Locale.getDefault().isO3Country,
        iso2
    ).displayCountry

    fun showCountryRegions(
        items: List<RegionModel>,
        continentName: String,
    ) {
        viewModelScope.launch {
            countryEntityList.find {
                items.any { region -> region.countryId == it.id }
            }?.let { country ->
                if (mapContent.value[country.iso2]?.regionList?.isEmpty() == false) return@let

                // Fetch size of each region from Geofabrik website.
                val regionSize = getSize(continentName, getCountryName(country.iso2))

                updateMapElement(
                    key = country.iso2,
                    item =
                    CountryParentItem(
                        regionList =
                        items.map { region ->

                            val key =
                                regionSize.keys.find { key ->
                                    region.names.any {
                                        it.value.equals(
                                            key,
                                            true,
                                        )
                                    }
                                }

                            RegionChildItem(
                                regionEntity = region,
                                isChecked = false,
                                loading = Loading(isLoading = false),
                                isCheckBoxEnabled = getDownloadQueueMapSize() < MAX_REGIONS_TO_DOWNLOAD,
                                size = regionSize[key] ?: "",
                            )
                        },
                        size =
                        mapContent.value[country.iso2]?.size
                            ?: "",
                        isLoading = false,
                    ),
                )
            }
        }
    }

    private suspend fun getSize(
        continentName: String,
        countryName: String? = null,
    ): Map<String, String> =
        countryDataExtractSizeRepository.fetchSize(
            continentName.lowercase().replace("[&\\s]+".toRegex(), "-"),
            countryName?.lowercase(),
        )

    fun showDownloadProgressBar(showLoading: Boolean, progress: Int? = null) {
        mapContent.value.forEach { mutableEntry ->
            val regionChildItemList =
                mutableEntry.value.regionList.map {
                    if (it.isChecked) {
                        RegionChildItem(
                            regionEntity = it.regionEntity,
                            isChecked = true,
                            loading = Loading(isLoading = showLoading, progress = progress),
                            isCheckBoxEnabled = it.isCheckBoxEnabled,
                            size = it.size,
                        )
                    } else {
                        RegionChildItem(
                            regionEntity = it.regionEntity,
                            isChecked = false,
                            loading = Loading(isLoading = false),
                            isCheckBoxEnabled = it.isCheckBoxEnabled,
                            size = it.size,
                        )
                    }
                }

            updateMapElement(
                mutableEntry.key,
                CountryParentItem(
                    regionList = regionChildItemList,
                    size = mapContent.value[mutableEntry.key]?.copy()?.size ?: "",
                    isLoading = false,
                ),
            )
        }
    }

    fun getRegionFromQueue(): Pair<Int, String> {
        var regions: Pair<Int, String> = -1 to ""

        downloadQueueMap.keys.firstOrNull()?.let { key ->
            mapContent.value[mapContent.value.getKeyByIndex(key)]?.let { countryParentItem ->
                val region = countryParentItem.regionList[downloadQueueMap.values.first().first()]

                val regionId = region.regionEntity.id
                val regionName = region.regionEntity.names["name"] ?: ""

                regions = regionId to regionName
            }
        }
        return regions
    }

    fun getAssociatedCountries(
        continentId: Int,
        region: String,
    ) {
        viewModelScope.launch {
            if (countryEntityList.isNotEmpty()) return@launch
            continentRepository.getAssociatedCountries(continentId).firstOrNull()?.let {
                // Fetch size of each country from Geofabrik website.
                val countrySize = getSize(region)

                _countryEntityList.addAll(
                    it.countries.sortedBy { country -> getCountryName(country.iso2) },
                )

                countryEntityList.forEach { country ->
                    updateMapElement(
                        key = country.iso2,
                        item =
                        CountryParentItem(
                            regionList = emptyList(),
                            size = countrySize[country.iso2] ?: "",
                            isLoading = false,
                        ),
                    )
                }
            }
        }
    }
}