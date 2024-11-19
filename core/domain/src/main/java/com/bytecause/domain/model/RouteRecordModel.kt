package com.bytecause.domain.model

data class RouteRecordModel(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val distance: Double = 0.0,
    val startTime: Long = 0L,
    val dateCreated: Long = 0L,
    val points: List<Pair<Double, Double>> = emptyList(),
    val speed: Map<Pair<Double, Double>, Float> = emptyMap()
)