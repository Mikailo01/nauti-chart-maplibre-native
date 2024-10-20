package com.bytecause.map.ui.event

import com.bytecause.map.ui.model.BottomSheetType

sealed interface AnchorageAlarmSettingsEvent {
    data class OnAnchorageVisibilityChange(val value: Boolean) : AnchorageAlarmSettingsEvent
    data class OnSelectedIntervalValueChange(val value: Int) :
        AnchorageAlarmSettingsEvent

    data class OnAnchorageHistoryItemClick(val id: String) : AnchorageAlarmSettingsEvent
    data class OnRemoveAnchorageHistoryItem(val id: String) : AnchorageAlarmSettingsEvent
    data class OnShowBottomSheet(val type: BottomSheetType?) : AnchorageAlarmSettingsEvent
    data class OnTrackMovementStateChange(val boolean: Boolean) : AnchorageAlarmSettingsEvent
    data class OnTrackBatteryStateChange(val boolean: Boolean) : AnchorageAlarmSettingsEvent
    data class OnClearHistoryConfirmDialogStateChange(val value: Boolean) :
        AnchorageAlarmSettingsEvent

    data object OnAnchorageHistoryDeletionIntervalClick : AnchorageAlarmSettingsEvent
    data object OnDeleteAnchorageHistory : AnchorageAlarmSettingsEvent
    data object OnToggleEditMode : AnchorageAlarmSettingsEvent
    data object OnNavigateBack : AnchorageAlarmSettingsEvent
}