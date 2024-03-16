package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.util.SimpleOverpassQueryBuilder
import org.osmdroid.util.GeoPoint

data class PoiQueryEntity(
    val category: List<String>,
    val radius: Int,
    val position: GeoPoint,
    val query: SimpleOverpassQueryBuilder
)