package com.bytecause.map.ui.effect

sealed interface AnchorageAlarmSettingsEffect {
    data object NavigateBack : AnchorageAlarmSettingsEffect
    data class AnchorageHistoryItemClick(val id: String) : AnchorageAlarmSettingsEffect
}