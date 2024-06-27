package com.bytecause.util.mappers

import com.bytecause.domain.model.LatLngModel
import org.maplibre.android.geometry.LatLng

fun LatLng.asLatLngModel(): LatLngModel = LatLngModel(latitude, longitude)