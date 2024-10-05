package com.bytecause.map.ui.effect

sealed interface AnchorageAlarmSettingsEffect {
    data object NavigateBack : AnchorageAlarmSettingsEffect
    data object OnShowIntervalBottomSheet : AnchorageAlarmSettingsEffect
}