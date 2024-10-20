package com.bytecause.map.ui.effect

import com.bytecause.map.ui.model.AnchorageHistoryDeletionInterval

sealed interface AnchorageAlarmSettingsEffect {
    data object NavigateBack : AnchorageAlarmSettingsEffect
    data class AnchorageHistoryItemClick(val id: String) : AnchorageAlarmSettingsEffect
    data class AnchorageHistoryDeletionIntervalClick(val intervalType: AnchorageHistoryDeletionInterval) :
        AnchorageAlarmSettingsEffect
}