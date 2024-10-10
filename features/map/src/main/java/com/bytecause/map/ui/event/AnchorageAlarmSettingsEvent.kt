package com.bytecause.map.ui.event

import com.bytecause.map.ui.state.BottomSheetType

sealed interface AnchorageAlarmSettingsEvent {
    data class OnAnchorageVisibilityChange(val value: Boolean) : AnchorageAlarmSettingsEvent
    data class OnSelectedIntervalValueChange(val value: Int) :
        AnchorageAlarmSettingsEvent
    data class OnAnchorageHistoryItemClick(val id: String) : AnchorageAlarmSettingsEvent
    data class OnRemoveAnchorageHistoryItem(val id: String) : AnchorageAlarmSettingsEvent
    data class OnShowBottomSheet(val type: BottomSheetType?) : AnchorageAlarmSettingsEvent

    data object OnToggleEditMode : AnchorageAlarmSettingsEvent
    data object OnNavigateBack : AnchorageAlarmSettingsEvent
}