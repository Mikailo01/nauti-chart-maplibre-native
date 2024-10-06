package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import com.bytecause.map.ui.effect.AnchorageAlarmSettingsEffect
import com.bytecause.map.ui.event.AnchorageAlarmSettingsEvent
import com.bytecause.map.ui.event.IntervalType
import com.bytecause.map.ui.mappers.asAnchorageHistoryUiModel
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.state.AnchorageAlarmSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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
            loadAnchorageHistory()
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

    private fun observeUiChanges() {
        observeJob?.cancel()
        observeJob = combine(
            anchoragesAlarmPreferencesRepository.getAlarmDelay(),
            anchoragesAlarmPreferencesRepository.getAnchorageLocationsVisible(),
            anchoragesAlarmPreferencesRepository.getMaxUpdateInterval(),
            anchoragesAlarmPreferencesRepository.getMinUpdateInterval()
        ) { alarmDelay, anchorageLocationsVisible, maxUpdateInterval, minUpdateInterval ->
            _uiState.update {
                it.copy(
                    maxGpsUpdateInterval = (maxUpdateInterval / 1000).toInt(),
                    minGpsUpdateInterval = (minUpdateInterval / 1000).toInt(),
                    alarmDelay = (alarmDelay / 1000).toInt(),
                    areAnchorageLocationsVisible = anchorageLocationsVisible
                )
            }
        }
            .launchIn(viewModelScope)
    }

    fun uiEventHandler(event: AnchorageAlarmSettingsEvent) {
        when (event) {
            AnchorageAlarmSettingsEvent.OnNavigateBack -> sendEffect(AnchorageAlarmSettingsEffect.NavigateBack)
            AnchorageAlarmSettingsEvent.OnMaximumUpdateIntervalClick -> onGpsRowClick(IntervalType.MAX_UPDATE_INTERVAL)
            AnchorageAlarmSettingsEvent.OnAlarmDelayClick -> onGpsRowClick(IntervalType.ALARM_DELAY)
            AnchorageAlarmSettingsEvent.OnMinimumUpdateIntervalClick -> onGpsRowClick(IntervalType.MIN_UPDATE_INTERVAL)

            is AnchorageAlarmSettingsEvent.OnAnchorageVisibilityChange -> onAnchorageVisibilityChange(
                event.value
            )

            is AnchorageAlarmSettingsEvent.OnSelectedIntervalValueChange -> onSelectedIntervalValueChange(
                intervalType = event.type,
                interval = event.value
            )

            is AnchorageAlarmSettingsEvent.OnUpdateIntervalType -> onUpdateIntervalType(event.type)
            is AnchorageAlarmSettingsEvent.OnAnchorageHistoryItemClick -> sendEffect(
                AnchorageAlarmSettingsEffect.AnchorageHistoryItemClick(event.id)
            )
        }
    }

    private fun sendEffect(effect: AnchorageAlarmSettingsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun loadAnchorageHistory() {
        viewModelScope.launch {
            getAnchorageHistoryList().firstOrNull()?.let {
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

    private fun onGpsRowClick(type: IntervalType) {
        // save interval type to be able to infer which property should be updated
        _uiState.update {
            it.copy(selectedIntervalType = type)
        }
        sendEffect(AnchorageAlarmSettingsEffect.OnShowNumberPickerBottomSheet)
    }

    private fun onUpdateIntervalType(intervalType: IntervalType) {
        _uiState.update {
            it.copy(selectedIntervalType = intervalType)
        }
    }

    private fun onSelectedIntervalValueChange(intervalType: IntervalType, interval: Int) {
        viewModelScope.launch {
            when (intervalType) {
                IntervalType.MAX_UPDATE_INTERVAL -> {
                    anchoragesAlarmPreferencesRepository.saveMaxUpdateInterval((interval * 1000).toLong())
                }

                IntervalType.MIN_UPDATE_INTERVAL -> {
                    anchoragesAlarmPreferencesRepository.saveMinUpdateInterval((interval * 1000).toLong())
                }

                IntervalType.ALARM_DELAY -> {
                    anchoragesAlarmPreferencesRepository.saveAlarmDelay((interval * 1000).toLong())
                }
            }

            _uiState.update {
                it.copy(selectedIntervalType = null)
            }
            sendEffect(AnchorageAlarmSettingsEffect.OnHideNumberPickerBottomSheet)
        }
    }
}