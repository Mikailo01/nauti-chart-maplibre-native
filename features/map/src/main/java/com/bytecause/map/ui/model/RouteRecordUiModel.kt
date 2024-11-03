package com.bytecause.map.ui.model

data class RouteRecordUiModel(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val distance: Double = 0.0,
    val startTime: Long = 0L,
    val duration: Long = 0L,
    val dateCreated: Long = 0L,
    val points: List<Pair<Double, Double>> = emptyList()
)