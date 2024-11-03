package com.bytecause.map.ui.model

data class TrackRouteServiceState(
    val isRunning: Boolean = false,
    val capturedPoints: List<Pair<Double, Double>> = emptyList()
)