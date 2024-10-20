package com.bytecause.data.model

data class RunningAnchorageAlarm(
    val isRunning: Boolean = false,
    val radius: Float = 5f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)