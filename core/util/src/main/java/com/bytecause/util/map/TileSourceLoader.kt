package com.bytecause.util.map

import com.bytecause.domain.tilesources.DefaultTileSources.OPEN_SEA_MAP
import com.bytecause.domain.tilesources.DefaultTileSources.SATELLITE
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.util.map.GeoJsonLoader.loadGeoJsonFromAssets
import com.bytecause.util.map.GeoJsonLoader.unloadGeoJson
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet


object TileSourceLoader {

    private const val MAIN_LAYER_ID = "main-tile-layer"
    private const val ADDITIONAL_LAYER_ID = "additional-tile-layer"
    private const val TILE_JSON = "tileset"
    private const val WORLD_BOUNDARIES_GEOJSON_NAME = "country_boundaries.geojson"

    fun loadRasterTileSource(style: Style, tileSource: TileSources.Raster) {
        style.apply {
            val existingSource: RasterSource? =
                getSourceAs(
                    when (tileSource) {
                        is TileSources.Raster.Default -> tileSource.id.name
                        is TileSources.Raster.Custom.Online -> tileSource.name
                        is TileSources.Raster.Custom.Offline -> tileSource.name
                        else -> return
                    },
                )
            if (existingSource != null) {
                loadRasterTileLayer(style, tileSource)
            } else {
                addSource(
                    createRasterSource(tileSource),
                )

                getSourceAs<RasterSource?>(OPEN_SEA_MAP.id.name) ?: run {
                    addSource(createRasterSource(OPEN_SEA_MAP))
                }

                loadRasterTileLayer(style, tileSource)
            }
        }
    }

    private fun loadRasterTileLayer(
        style: Style,
        tileSource: TileSources.Raster,
        // Additional transparent layer which is displayed above the main layer
        additionalTileSource: TileSources? = OPEN_SEA_MAP,
    ) {
        style.apply {
            handleRasterLayer(tileSource, MAIN_LAYER_ID)

            additionalTileSource?.let {
                val additionalTileSourceAsDefault = it as TileSources.Raster.Default
                handleRasterLayer(additionalTileSourceAsDefault, ADDITIONAL_LAYER_ID)
            }

            if (tileSource == SATELLITE) {
                loadGeoJsonFromAssets(
                    geoJsonName = WORLD_BOUNDARIES_GEOJSON_NAME,
                    index =
                    layers.indexOfFirst {
                        it.id == MAIN_LAYER_ID
                    } + 1,
                )
            } else {
                unloadGeoJson(WORLD_BOUNDARIES_GEOJSON_NAME)
            }
        }
    }

    private fun Style.handleRasterLayer(
        tileSource: TileSources.Raster,
        layerId: String,
    ) {
        val existingLayer: RasterLayer? = getLayerAs(layerId)

        if (existingLayer != null) {
            val existingLayerIndex = layers.indexOfFirst { it.id == existingLayer.id }
            removeLayer(existingLayer)

            val updatedLayer = createRasterLayer(existingLayer.id, tileSource)

            if (existingLayerIndex != -1 && existingLayerIndex < layers.size) {
                addLayerAt(updatedLayer, existingLayerIndex)
            } else {
                addLayer(updatedLayer)
            }
            // layer is not present yet, so add it
        } else addLayer(createRasterLayer(layerId, tileSource))
    }

    private fun createRasterSource(tileSource: TileSources.Raster): RasterSource {
        return when (tileSource) {
            is TileSources.Raster.Default -> {
                RasterSource(
                    tileSource.id.name,
                    TileSet(
                        TILE_JSON,
                        *tileSource.url,
                    ),
                    tileSource.tileSize,
                )
            }

            is TileSources.Raster.Custom -> {
                when (tileSource) {
                    is TileSources.Raster.Custom.Online -> {
                        RasterSource(
                            tileSource.name,
                            TileSet(
                                TILE_JSON,
                                tileSource.url,
                            ),
                            tileSource.tileSize,
                        )
                    }

                    is TileSources.Raster.Custom.Offline -> {
                        RasterSource(
                            tileSource.name,
                            "mbtiles:///${tileSource.filePath}",
                            tileSource.tileSize
                        )
                    }
                }
            }
        }
    }

    private fun createRasterLayer(
        layerId: String,
        tileSource: TileSources.Raster,
    ): RasterLayer {
        return when (tileSource) {
            is TileSources.Raster.Default ->
                RasterLayer(layerId, tileSource.id.name).apply {
                    minZoom = tileSource.minZoom
                    maxZoom = tileSource.maxZoom
                }

            is TileSources.Raster.Custom.Online ->
                RasterLayer(layerId, tileSource.name).apply {
                    minZoom = tileSource.minZoom
                    maxZoom = tileSource.maxZoom
                }

            is TileSources.Raster.Custom.Offline -> {
                RasterLayer(layerId, tileSource.name).apply {
                    minZoom = tileSource.minZoom
                    maxZoom = tileSource.maxZoom
                }
            }
        }
    }
}