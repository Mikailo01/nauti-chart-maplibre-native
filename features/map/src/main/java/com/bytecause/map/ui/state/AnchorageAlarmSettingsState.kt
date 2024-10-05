package com.bytecause.map.ui.state

import androidx.compose.runtime.Immutable
import com.bytecause.map.ui.event.IntervalType

@Immutable
data class AnchorageAlarmSettingsState(
    val selectedIntervalType: IntervalType? = null,
    val maxGpsUpdateInterval: Int = 0,
    val minGpsUpdateInterval: Int = 0,
    val alarmDelay: Int = 0,
    val areAnchorageLocationsVisible: Boolean = false
)