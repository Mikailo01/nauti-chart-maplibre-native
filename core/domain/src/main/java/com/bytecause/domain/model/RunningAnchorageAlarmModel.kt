package com.bytecause.domain.model

data class RunningAnchorageAlarmModel(
    val isRunning: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 0.0f
)