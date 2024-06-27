package com.bytecause.domain.model


data class PoiQueryModel(
    val category: List<String>,
    val radius: Int,
    val position: LatLngModel,
    val query: String,
    val appliedFilters: Map<String, List<String>>?
)