package com.bytecause.map.ui.event

sealed interface TrackRouteBottomSheetEvent {
    data object OnStartForegroundService : TrackRouteBottomSheetEvent
    data object OnStopForegroundService : TrackRouteBottomSheetEvent
    data object OnToggleEditMode : TrackRouteBottomSheetEvent
    data object OnFilterClick : TrackRouteBottomSheetEvent
    data object OnSortClick : TrackRouteBottomSheetEvent
    data object OnCloseBottomSheet : TrackRouteBottomSheetEvent
    data object OnToggleRenderAllTracksSwitch: TrackRouteBottomSheetEvent
    data object OnNavigateBack : TrackRouteBottomSheetEvent
    data class OnRemoveItem(val id: Long) : TrackRouteBottomSheetEvent
    data class OnItemClick(val id: Long) : TrackRouteBottomSheetEvent
}