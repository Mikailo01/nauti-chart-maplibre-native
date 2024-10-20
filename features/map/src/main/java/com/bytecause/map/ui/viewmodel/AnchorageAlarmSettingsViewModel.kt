package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.model.AnchorageHistoryDeletionIntervalModel
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import com.bytecause.map.ui.effect.AnchorageAlarmSettingsEffect
import com.bytecause.map.ui.event.AnchorageAlarmSettingsEvent
import com.bytecause.map.ui.mappers.asAnchorageHistoryUiModel
import com.bytecause.map.ui.model.AnchorageHistoryDeletionInterval
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.BottomSheetType
import com.bytecause.map.ui.state.AnchorageAlarmSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnchorageAlarmSettingsViewModel @Inject constructor(
    private val anchoragesAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository,
    private val anchorageHistoryRepository: AnchorageHistoryRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<AnchorageAlarmSettingsState> =
        MutableStateFlow(AnchorageAlarmSettingsState())
    val uiState: StateFlow<AnchorageAlarmSettingsState> = _uiState
        .onStart {
            observeUiChanges()
            observeAnchorageHistory()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AnchorageAlarmSettingsState()
        )

    private val _effect: Channel<AnchorageAlarmSettingsEffect> =
        Channel(capacity = Channel.CONFLATED)
    val effect: Flow<AnchorageAlarmSettingsEffect> = _effect.receiveAsFlow()

    private var observeJob: Job? = null
    private var anchorageHistoryObserveJob: Job? = null

    private fun observeUiChanges() {
        observeJob?.cancel()
        observeJob = combine(
            listOf(
                anchoragesAlarmPreferencesRepository.getAlarmDelay(),
                anchoragesAlarmPreferencesRepository.getAnchorageLocationsVisible(),
                anchoragesAlarmPreferencesRepository.getMaxUpdateInterval(),
                anchoragesAlarmPreferencesRepository.getMinUpdateInterval(),
                anchoragesAlarmPreferencesRepository.getTrackMovementState(),
                anchoragesAlarmPreferencesRepository.getTrackBatteryState(),
                anchoragesAlarmPreferencesRepository.getAnchorageHistoryDeletionInterval()
            )
        ) {
            val alarmDelay = it[0] as Long
            val anchorageLocationsVisible = it[1] as Boolean
            val maxUpdateInterval = it[2] as Long
            val minUpdateInterval = it[3] as Long
            val trackMovementState = it[4] as Boolean
            val trackBatteryState = it[5] as Boolean
            val anchorageHistoryDeletionInterval = it[6] as AnchorageHistoryDeletionIntervalModel

            _uiState.update { state ->
                state.copy(
                    maxGpsUpdateInterval = (maxUpdateInterval / 1000).toInt(),
                    minGpsUpdateInterval = (minUpdateInterval / 1000).toInt(),
                    alarmDelay = (alarmDelay / 1000).toInt(),
                    areAnchorageLocationsVisible = anchorageLocationsVisible,
                    trackMovement = trackMovementState,
                    trackBatteryState = trackBatteryState,
                    anchorageHistoryDeletionInterval = AnchorageHistoryDeletionInterval.valueOf(
                        anchorageHistoryDeletionInterval.name
                    )
                )
            }
        }
            .launchIn(viewModelScope)
    }

    fun uiEventHandler(event: AnchorageAlarmSettingsEvent) {
        when (event) {

            is AnchorageAlarmSettingsEvent.OnAnchorageVisibilityChange -> onAnchorageVisibilityChange(
                event.value
            )

            is AnchorageAlarmSettingsEvent.OnSelectedIntervalValueChange -> onSelectedIntervalValueChange(
                interval = event.value
            )

            is AnchorageAlarmSettingsEvent.OnAnchorageHistoryItemClick -> sendEffect(
                AnchorageAlarmSettingsEffect.AnchorageHistoryItemClick(event.id)
            )

            is AnchorageAlarmSettingsEvent.OnShowBottomSheet -> onShowBottomSheet(event.type)
            is AnchorageAlarmSettingsEvent.OnRemoveAnchorageHistoryItem -> onRemoveAnchorageHistoryItem(
                event.id
            )

            is AnchorageAlarmSettingsEvent.OnTrackBatteryStateChange -> onTrackBatteryStateChange(
                event.boolean
            )

            is AnchorageAlarmSettingsEvent.OnTrackMovementStateChange -> onTrackMovementChange(event.boolean)
            is AnchorageAlarmSettingsEvent.OnClearHistoryConfirmDialogStateChange -> onClearHistoryConfirmDialogStateChange(
                event.value
            )

            AnchorageAlarmSettingsEvent.OnNavigateBack -> sendEffect(AnchorageAlarmSettingsEffect.NavigateBack)
            AnchorageAlarmSettingsEvent.OnToggleEditMode -> onToggleEditMode()
            AnchorageAlarmSettingsEvent.OnDeleteAnchorageHistory -> onClearAnchorageHistory()
            AnchorageAlarmSettingsEvent.OnAnchorageHistoryDeletionIntervalClick -> onAnchorageHistoryDeletionIntervalClick()
        }
    }

    private fun sendEffect(effect: AnchorageAlarmSettingsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun observeAnchorageHistory() {
        anchorageHistoryObserveJob?.cancel()
        anchorageHistoryObserveJob = viewModelScope.launch {
            getAnchorageHistoryList().collect {
                _uiState.update { state ->
                    state.copy(anchorageHistory = it)
                }
            }
        }
    }

    private fun getAnchorageHistoryList(): Flow<List<AnchorageHistoryUiModel>> =
        anchorageHistoryRepository.getAnchorageHistoryList()
            .map { historyList ->
                historyList.anchorageHistoryList.map {
                    it.asAnchorageHistoryUiModel()
                }
                    .sortedWith(compareByDescending {
                        it.timestamp
                    })
            }

    private fun onAnchorageVisibilityChange(boolean: Boolean) {
        viewModelScope.launch {
            anchoragesAlarmPreferencesRepository.saveAnchorageLocationsVisible(boolean)
        }
    }

    private fun onShowBottomSheet(type: BottomSheetType?) {
        _uiState.update {
            it.copy(bottomSheetType = type)
        }
    }

    private fun onAnchorageHistoryDeletionIntervalClick() {
        viewModelScope.launch {
            val index =
                AnchorageHistoryDeletionInterval.valueOf(uiState.value.anchorageHistoryDeletionInterval.name).ordinal

            val interval =
                if (index != AnchorageHistoryDeletionInterval.entries.lastIndex) {
                    AnchorageHistoryDeletionInterval.entries[index + 1]
                } else AnchorageHistoryDeletionInterval.entries[0]

            anchoragesAlarmPreferencesRepository.saveAnchorageHistoryDeletionInterval(
                AnchorageHistoryDeletionIntervalModel.valueOf(interval.name)
            )

            sendEffect(
                AnchorageAlarmSettingsEffect.AnchorageHistoryDeletionIntervalClick(
                    interval
                )
            )
        }
    }

    private fun onClearHistoryConfirmDialogStateChange(b: Boolean) {
        _uiState.update {
            it.copy(showDeleteHistoryConfirmationDialog = b)
        }
    }

    private fun onClearAnchorageHistory() {
        viewModelScope.launch {
            anchorageHistoryRepository.clearAnchorageHistory()
            _uiState.update { it.copy(showDeleteHistoryConfirmationDialog = false) }
        }
    }

    private fun onTrackMovementChange(boolean: Boolean) {
        viewModelScope.launch {
            anchoragesAlarmPreferencesRepository.saveTrackMovementState(boolean)
        }
    }

    private fun onTrackBatteryStateChange(boolean: Boolean) {
        viewModelScope.launch {
            anchoragesAlarmPreferencesRepository.saveTrackBatteryState(boolean)
        }
    }

    private fun onToggleEditMode() {
        _uiState.update {
            it.copy(isEditMode = !it.isEditMode)
        }
    }

    private fun onRemoveAnchorageHistoryItem(id: String) {
        viewModelScope.launch {
            anchorageHistoryRepository.removeAnchorageHistory(id)
        }
    }

    private fun onSelectedIntervalValueChange(interval: Int) {
        viewModelScope.launch {
            when (uiState.value.bottomSheetType ?: return@launch) {
                BottomSheetType.MAX_UPDATE_INTERVAL -> {
                    anchoragesAlarmPreferencesRepository.saveMaxUpdateInterval((interval * 1000).toLong())
                }

                BottomSheetType.MIN_UPDATE_INTERVAL -> {
                    anchoragesAlarmPreferencesRepository.saveMinUpdateInterval((interval * 1000).toLong())
                }

                BottomSheetType.ALARM_DELAY -> {
                    anchoragesAlarmPreferencesRepository.saveAlarmDelay((interval * 1000).toLong())
                }
            }

            _uiState.update {
                it.copy(bottomSheetType = null)
            }
        }
    }
}