package com.bytecause.map.ui.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable

@Stable
data class TrackRouteMainContentState(
    val serviceRunning: Boolean = false,
    val isEditMode: Boolean = false,
    val isRenderAllTracksSwitchChecked: Boolean = false,
    val lazyListState: LazyListState = LazyListState(),
    val hasAnimationFinished: Boolean = false
)