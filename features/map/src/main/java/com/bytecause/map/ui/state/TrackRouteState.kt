package com.bytecause.map.ui.state

import com.bytecause.map.ui.model.TrackedRouteItem

data class TrackRouteState(
    val records: List<TrackedRouteItem> = emptyList(),
    val serviceRunning: Boolean = false,
    val isEditMode: Boolean = false,
    val isRenderAllTracksSwitchChecked: Boolean = false
)