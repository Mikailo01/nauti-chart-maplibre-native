package com.bytecause.map.ui.model

import android.graphics.drawable.Drawable
import org.maplibre.android.geometry.LatLng

data class MarkerInfoModel(
    val title: String,
    val type: String? = null,
    val iconImage: Drawable? = null,
    val propImages: List<Int> = emptyList(),
    val description: String? = null,
    val contacts: String? = null,
    val image: String? = null,
    val position: LatLng
)