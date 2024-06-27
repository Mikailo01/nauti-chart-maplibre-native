package com.bytecause.map.ui.model

import android.graphics.drawable.Drawable
import org.maplibre.android.geometry.LatLng

data class MarkerInfoModel(
    val title: String,
    val image: Drawable? = null,
    val description: String? = null,
    val position: LatLng
)