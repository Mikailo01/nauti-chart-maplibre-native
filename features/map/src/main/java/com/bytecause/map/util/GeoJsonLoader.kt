package com.bytecause.map.util

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.net.URI

object GeoJsonLoader {
    private const val WORLD_BOUNDARIES_LAYER = "world_boundaries_layer"

    fun Style.loadGeoJsonFromAssets(
        geoJsonName: String,
        index: Int = -1,
    ) {
        if (getSourceAs<GeoJsonSource>(geoJsonName) != null) return

        addSource(createGeoJsonSource(geoJsonName))
        // index not specified, add geojson layer to the end of the stack
        if (index == -1) {
            addLayer(
                createGeoJsonLayer(
                    lineLayerId = WORLD_BOUNDARIES_LAYER,
                    sourceId = geoJsonName,
                ),
            )
        }
        // index specified add geojson layer at specified index on the stack
        else {
            addLayerAt(
                createGeoJsonLayer(
                    lineLayerId = WORLD_BOUNDARIES_LAYER,
                    sourceId = geoJsonName,
                ),
                index,
            )
        }
    }

    fun Style.unloadGeoJson(geoJsonName: String) {
        if (getSourceAs<GeoJsonSource?>(geoJsonName) == null &&
            getLayerAs<LineLayer?>(WORLD_BOUNDARIES_LAYER) == null
        ) {
            return
        }

        removeLayer(WORLD_BOUNDARIES_LAYER)
        removeSource(geoJsonName)
    }

    private fun createGeoJsonSource(geoJsonName: String): GeoJsonSource =
        GeoJsonSource(geoJsonName, URI("asset://$geoJsonName"))

    private fun createGeoJsonLayer(
        lineLayerId: String,
        sourceId: String,
    ): LineLayer =
        LineLayer(lineLayerId, sourceId)
            .withProperties(
                PropertyFactory.lineOpacity(0.7f),
                PropertyFactory.lineWidth(2f),
                PropertyFactory.lineColor("#000000"),
            )
}
