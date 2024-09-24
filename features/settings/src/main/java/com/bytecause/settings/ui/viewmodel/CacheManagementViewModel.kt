package com.bytecause.settings.ui.viewmodel

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.abstractions.HarboursMetadataDatasetRepository
import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.abstractions.RegionRepository
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.VesselsMetadataDatasetRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.NetworkType
import com.bytecause.settings.ui.ConfirmationDialogType
import com.bytecause.settings.ui.UpdateInterval
import com.bytecause.settings.ui.event.CacheManagementEffect
import com.bytecause.settings.ui.event.CacheManagementEvent
import com.bytecause.settings.ui.model.RegionUiModel
import com.bytecause.settings.ui.state.CacheManagementState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

@HiltViewModel
class CacheManagementViewModel @Inject constructor(
    private val osmRegionMetadataDatasetRepository: OsmRegionMetadataDatasetRepository,
    private val harboursMetadataDatasetRepository: HarboursMetadataDatasetRepository,
    private val vesselsMetadataDatasetRepository: VesselsMetadataDatasetRepository,
    private val regionRepository: RegionRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<CacheManagementState> =
        MutableStateFlow(CacheManagementState())
    val uiState: StateFlow<CacheManagementState> = _uiState.asStateFlow()

    private val _effect = Channel<CacheManagementEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private var uiReadyFlag: Boolean = false

    private fun startObservingRegionDatasets() {
        // get regions datasets and their last update timestamps
        combine(
            regionRepository.getAllDownloadedRegions(),
            osmRegionMetadataDatasetRepository.getAllDatasets()
        ) { downloadedRegions, regionDatasets ->

            _uiState.update { state ->
                state.copy(
                    downloadedRegions = downloadedRegions.associate { region ->

                        val timestamp =
                            regionDatasets.find { dataset -> dataset.id == region.id }?.timestamp

                        region.id to RegionUiModel(
                            regionId = region.id,
                            names = region.names,
                            timestamp = timestamp ?: 0L,
                            isUpdating = state.downloadedRegions[region.id]?.isUpdating ?: false,
                            progress = state.downloadedRegions[region.id]?.progress ?: -1
                        )
                    }
                )
            }

            uiReadyFlag = true
        }.launchIn(viewModelScope)
    }

    private fun startObservingHarboursDataset() {
        // get harbours dataset and last update timestamp
        viewModelScope.launch {
            harboursMetadataDatasetRepository.getDataset().collect { dataset ->
                _uiState.update {
                    it.copy(
                        harboursModel = it.harboursModel.copy(
                            timestamp = dataset?.timestamp?.let { timestamp ->
                                DateFormat.format(
                                    DEFAULT_DATE_FORMAT,
                                    timestamp
                                ).toString()
                            } ?: ""
                        )
                    )
                }
            }
        }
    }

    private fun startObservingVesselsDataset() {
        // get vessels dataset and last update timestamp
        viewModelScope.launch {
            vesselsMetadataDatasetRepository.getDataset().collect { dataset ->
                _uiState.update {
                    it.copy(
                        vesselsTimestamp = dataset?.timestamp?.let { timestamp ->
                            DateFormat.format(
                                DEFAULT_DATE_FORMAT,
                                timestamp
                            ).toString()
                        } ?: ""
                    )
                }
            }
        }
    }

    private fun startObservingSelectedHarboursUpdateInterval() {
        // get selected harbours update interval from preferences datastore
        viewModelScope.launch {
            userPreferencesRepository.getHarboursUpdateInterval().collect { interval ->
                _uiState.update {
                    it.copy(
                        harboursModel = it.harboursModel.copy(
                            harboursUpdateInterval = when {
                                UpdateInterval.OneWeek().interval == interval -> UpdateInterval.OneWeek()
                                UpdateInterval.OneMonth().interval == interval -> UpdateInterval.OneMonth()
                                else -> UpdateInterval.TwoWeeks()
                            }
                        ),
                    )
                }
            }
        }
    }

    private fun startObservingSelectedPoiUpdateInterval() {
        // get selected region pois update interval from preferences datastore
        viewModelScope.launch {
            userPreferencesRepository.getPoiUpdateInterval().collect { interval ->
                _uiState.update {
                    it.copy(
                        poiUpdateInterval = when {
                            UpdateInterval.OneWeek().interval == interval -> UpdateInterval.OneWeek()
                            UpdateInterval.OneMonth().interval == interval -> UpdateInterval.OneMonth()
                            else -> UpdateInterval.TwoWeeks()
                        }
                    )
                }
            }
        }
    }

    private fun startObservingAutoUpdatesNetworkPreference() {
        viewModelScope.launch {
            userPreferencesRepository.getAutoUpdatesNetworkPreference().collect { preference ->
                _uiState.update {
                    it.copy(autoUpdateNetworkType = preference)
                }
            }
        }
    }

    init {
        startObservingRegionDatasets()
        startObservingHarboursDataset()
        startObservingVesselsDataset()

        startObservingSelectedHarboursUpdateInterval()
        startObservingSelectedPoiUpdateInterval()
        startObservingAutoUpdatesNetworkPreference()

        viewModelScope.launch {
            // wait until the UI is ready
            while (!uiReadyFlag) {
                delay(20)
            }

            ServiceApiResultListener.eventFlow.collect { event ->
                when (event) {
                    is ServiceEvent.HarboursUpdate -> {
                        when (event.result) {
                            is ApiResult.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        harboursModel = it.harboursModel.copy(
                                            isUpdating = false,
                                            progress = -1
                                        )
                                    )
                                }

                                sendEffect(CacheManagementEffect.HarboursUpdateFailure)
                            }

                            is ApiResult.Progress -> {
                                event.result.progress?.let { progress ->
                                    _uiState.update {
                                        it.copy(
                                            harboursModel = it.harboursModel.copy(
                                                progress = progress
                                            )
                                        )
                                    }
                                }
                            }

                            is ApiResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        harboursModel = it.harboursModel.copy(
                                            isUpdating = false,
                                            progress = -1
                                        )
                                    )
                                }

                                sendEffect(CacheManagementEffect.HarboursUpdateSuccess)
                            }
                        }
                    }

                    is ServiceEvent.RegionPoiDownload -> {
                        when (event.result) {
                            is ApiResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        downloadedRegions = it.downloadedRegions.toMutableMap()
                                            .apply {
                                                it.downloadedRegions[event.regionId]?.let { region ->
                                                    replace(
                                                        event.regionId,
                                                        region.copy(
                                                            isUpdating = false,
                                                            progress = -1
                                                        )
                                                    )
                                                }
                                            })
                                }

                                sendEffect(CacheManagementEffect.RegionUpdateSuccess)
                            }

                            is ApiResult.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        downloadedRegions = it.downloadedRegions.toMutableMap()
                                            .apply {
                                                it.downloadedRegions[event.regionId]?.let { region ->
                                                    replace(
                                                        event.regionId,
                                                        region.copy(
                                                            isUpdating = false,
                                                            progress = -1
                                                        )
                                                    )
                                                }
                                            })
                                }
                                sendEffect(CacheManagementEffect.RegionUpdateFailure)
                            }

                            is ApiResult.Progress -> {
                                event.result.progress?.let { progress ->
                                    _uiState.update {
                                        it.copy(
                                            downloadedRegions = it.downloadedRegions.toMutableMap()
                                                .apply {
                                                    it.downloadedRegions[event.regionId]?.let { region ->
                                                        replace(
                                                            event.regionId,
                                                            region.copy(
                                                                isUpdating = true,
                                                                progress = progress
                                                            )
                                                        )
                                                    }
                                                })
                                    }
                                }
                            }
                        }
                    }

                    is ServiceEvent.RegionPoiDownloadStarted -> {
                        _uiState.update {
                            it.copy(downloadedRegions = it.downloadedRegions.toMutableMap().apply {
                                it.downloadedRegions[event.regionId]?.let { region ->
                                    replace(event.regionId, region.copy(isUpdating = true))
                                }
                            })
                        }
                    }

                    is ServiceEvent.RegionPoiDownloadCancelled -> {
                        _uiState.update {
                            it.copy(
                                downloadedRegions = it.downloadedRegions.toMutableMap()
                                    .apply {
                                        it.downloadedRegions[event.regionId]?.let { region ->
                                            replace(
                                                event.regionId,
                                                region.copy(
                                                    isUpdating = false,
                                                    progress = -1
                                                )
                                            )
                                        }
                                    })
                        }
                    }

                    ServiceEvent.HarboursUpdateCancelled -> {
                        _uiState.update {
                            it.copy(
                                harboursModel = it.harboursModel.copy(
                                    isUpdating = false,
                                    progress = -1
                                )
                            )
                        }
                    }

                    ServiceEvent.HarboursUpdateStarted -> {
                        _uiState.update {
                            it.copy(harboursModel = it.harboursModel.copy(isUpdating = true))
                        }
                    }
                }
            }
        }
    }

    fun uiEventHandler(event: CacheManagementEvent) {
        when (event) {
            CacheManagementEvent.OnNavigateBack -> sendEffect(CacheManagementEffect.NavigateBack)
            CacheManagementEvent.OnClearSearchHistory -> onClearSearchHistory()
            CacheManagementEvent.OnClearHarbours -> onClearHarbours()
            CacheManagementEvent.OnClearVessels -> onClearVessels()
            is CacheManagementEvent.OnShowConfirmationDialog -> onShowConfirmationDialog(event.value)
            is CacheManagementEvent.OnDeleteRegion -> onDeleteRegion(event.regionId)
            is CacheManagementEvent.OnSetHarboursUpdateInterval -> onSetHarboursUpdateInterval(event.interval)
            is CacheManagementEvent.OnSetPoiUpdateInterval -> onSetPoiUpdateInterval(event.interval)
            is CacheManagementEvent.OnSetAutoUpdateNetworkTypePreference -> onSetAutoUpdateNetworkTypePreference(
                event.networkType
            )
        }
    }

    private fun sendEffect(effect: CacheManagementEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun onDeleteRegion(regionId: Int) {
        viewModelScope.launch {
            osmRegionMetadataDatasetRepository.deleteDataset(regionId)
            // reset download state
            regionRepository.getRegion(regionId).firstOrNull()?.let { region ->
                regionRepository.cacheRegions(listOf(region.copy(isDownloaded = false)))
            }
            onShowConfirmationDialog(null)
        }
    }

    private fun onSetPoiUpdateInterval(interval: UpdateInterval) {
        viewModelScope.launch {
            val intervalAsLong = when (interval) {
                is UpdateInterval.OneWeek -> {
                    interval.interval
                }

                is UpdateInterval.TwoWeeks -> {
                    interval.interval
                }

                is UpdateInterval.OneMonth -> {
                    interval.interval
                }
            }

            userPreferencesRepository.savePoiUpdateInterval(intervalAsLong)
        }
    }

    private fun onSetAutoUpdateNetworkTypePreference(networkType: NetworkType) {
        viewModelScope.launch {
            userPreferencesRepository.saveAutoUpdateNetworkPreference(networkType)
        }
    }

    private fun onSetHarboursUpdateInterval(interval: UpdateInterval) {
        viewModelScope.launch {
            val intervalAsLong = when (interval) {
                is UpdateInterval.OneWeek -> {
                    interval.interval
                }

                is UpdateInterval.TwoWeeks -> {
                    interval.interval
                }

                is UpdateInterval.OneMonth -> {
                    interval.interval
                }
            }

            userPreferencesRepository.saveHarboursUpdateInterval(intervalAsLong)
        }
    }

    private fun onShowConfirmationDialog(type: ConfirmationDialogType?) {
        _uiState.update {
            it.copy(showConfirmationDialog = type)
        }
    }

    private fun onClearVessels() {
        viewModelScope.launch {
            vesselsMetadataDatasetRepository.deleteDataset()
            onShowConfirmationDialog(null)
        }
    }

    private fun onClearHarbours() {
        viewModelScope.launch {
            harboursMetadataDatasetRepository.deleteDataset()
            onShowConfirmationDialog(null)
        }
    }

    private fun onClearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearRecentlySearchedPlaces()
            onShowConfirmationDialog(null)
        }
    }
}