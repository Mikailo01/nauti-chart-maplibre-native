package com.bytecause.map.ui.state

import androidx.compose.runtime.Immutable
import com.bytecause.map.ui.model.AnchorageHistoryUiModel

enum class BottomSheetType {
    MAX_UPDATE_INTERVAL,
    MIN_UPDATE_INTERVAL,
    ALARM_DELAY
}

@Immutable
data class AnchorageAlarmSettingsState(
    val maxGpsUpdateInterval: Int = 0,
    val minGpsUpdateInterval: Int = 0,
    val alarmDelay: Int = 0,
    val areAnchorageLocationsVisible: Boolean = false,
    val bottomSheetType: BottomSheetType? = null,
    val anchorageHistory: List<AnchorageHistoryUiModel> = emptyList()
)