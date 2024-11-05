package com.bytecause.map.ui.state

import androidx.compose.runtime.Stable

@Stable
data class TrackRouteMainContentState(
    val serviceRunning: Boolean = false,
    val isEditMode: Boolean = false,
    val chooseFilter: Boolean = false,
    val chooseSorter: Boolean = false,
    val isRenderAllTracksSwitchChecked: Boolean = false
)