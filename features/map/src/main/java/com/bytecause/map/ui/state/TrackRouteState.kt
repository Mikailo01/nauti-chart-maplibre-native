package com.bytecause.map.ui.state

data class TrackRouteState(
    val serviceRunning: Boolean = false,
    val isEditMode: Boolean = false,
    val isRenderAllTracksSwitchChecked: Boolean = false
)