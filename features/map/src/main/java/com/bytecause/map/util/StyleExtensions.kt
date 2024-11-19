package com.bytecause.map.util

import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.Source

object StyleExtensions {

    fun Style.cleanUpSourceAndLayer(sourceId: String, layerId: String) {
        getSourceAs<Source>(sourceId)?.run {
            removeLayer(layerId)
            removeSource(sourceId)
        }
    }
}