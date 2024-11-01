package com.bytecause.map.ui.model

data class TrackedRouteItem(
    val id: Long,
    val name: String,
    val description: String,
    val distance: Double,
    val duration: Long,
    val dateCreated: Long
)