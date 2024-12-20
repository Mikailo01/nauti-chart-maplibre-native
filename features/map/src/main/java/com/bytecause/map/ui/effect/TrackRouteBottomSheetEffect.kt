package com.bytecause.map.ui.effect

interface TrackRouteBottomSheetEffect {
    data object StartForegroundService : TrackRouteBottomSheetEffect
    data object CloseBottomSheet : TrackRouteBottomSheetEffect
    data object ShowConfirmStopRouteTrackDialog : TrackRouteBottomSheetEffect
}