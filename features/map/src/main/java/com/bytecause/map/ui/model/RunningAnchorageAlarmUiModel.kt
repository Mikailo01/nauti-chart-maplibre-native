package com.bytecause.map.ui.model

data class RunningAnchorageAlarmUiModel(
    val isRunning: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 0f
)