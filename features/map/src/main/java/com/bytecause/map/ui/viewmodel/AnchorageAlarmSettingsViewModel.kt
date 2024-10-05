package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.map.ui.effect.AnchorageAlarmSettingsEffect
import com.bytecause.map.ui.event.AnchorageAlarmSettingsEvent
import com.bytecause.map.ui.event.IntervalType
import com.bytecause.map.ui.state.AnchorageAlarmSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnchorageAlarmSettingsViewModel @Inject constructor(
    private val anchoragesAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<AnchorageAlarmSettingsState> =
        MutableStateFlow(AnchorageAlarmSettingsState())
    val uiState: StateFlow<AnchorageAlarmSettingsState> = _uiState.asStateFlow()

    private val _effect: Channel<AnchorageAlarmSettingsEffect> =
        Channel(capacity = Channel.CONFLATED)
    val effect: Flow<AnchorageAlarmSettingsEffect> = _effect.receiveAsFlow()

    init {
        combine(
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
            AnchorageAlarmSettingsEvent.OnMaximumUpdateIntervalClick -> {
                sendEffect(AnchorageAlarmSettingsEffect.OnShowIntervalBottomSheet)
            }

            AnchorageAlarmSettingsEvent.OnAlarmDelayClick -> sendEffect(AnchorageAlarmSettingsEffect.OnShowIntervalBottomSheet)
            AnchorageAlarmSettingsEvent.OnMinimumUpdateIntervalClick -> sendEffect(
                AnchorageAlarmSettingsEffect.OnShowIntervalBottomSheet
            )

            is AnchorageAlarmSettingsEvent.OnAnchorageVisibilityChange -> onAnchorageVisibilityChange(
                event.value
            )

            is AnchorageAlarmSettingsEvent.OnSelectedIntervalValueChange -> onSelectedIntervalValueChange(
                intervalType = event.type,
                interval = event.value
            )

            is AnchorageAlarmSettingsEvent.OnUpdateIntervalType -> onUpdateIntervalType(event.type)
        }
    }

    private fun sendEffect(effect: AnchorageAlarmSettingsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun onAnchorageVisibilityChange(boolean: Boolean) {
        viewModelScope.launch {
            anchoragesAlarmPreferencesRepository.saveAnchorageLocationsVisible(boolean)
        }
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
        }
    }
}