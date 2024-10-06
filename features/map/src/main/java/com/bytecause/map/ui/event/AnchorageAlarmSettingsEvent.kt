package com.bytecause.map.ui.event

enum class IntervalType {
    MAX_UPDATE_INTERVAL,
    MIN_UPDATE_INTERVAL,
    ALARM_DELAY
}

sealed interface AnchorageAlarmSettingsEvent {
    data class OnAnchorageVisibilityChange(val value: Boolean) : AnchorageAlarmSettingsEvent
    data class OnSelectedIntervalValueChange(val type: IntervalType, val value: Int) :
        AnchorageAlarmSettingsEvent

    data class OnUpdateIntervalType(val type: IntervalType) : AnchorageAlarmSettingsEvent
    data class OnAnchorageHistoryItemClick(val id: String) : AnchorageAlarmSettingsEvent

    data object OnNavigateBack : AnchorageAlarmSettingsEvent
    data object OnMaximumUpdateIntervalClick : AnchorageAlarmSettingsEvent
    data object OnMinimumUpdateIntervalClick : AnchorageAlarmSettingsEvent
    data object OnAlarmDelayClick : AnchorageAlarmSettingsEvent
}