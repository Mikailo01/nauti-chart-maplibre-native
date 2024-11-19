package com.bytecause.map.ui.model

sealed interface TrackRouteContentType {
    data object Sort : TrackRouteContentType
    data object Filter : TrackRouteContentType
    data object Main : TrackRouteContentType
    data object Detail : TrackRouteContentType
}