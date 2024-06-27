package com.bytecause.domain.tilesources

object DefaultTileSources {
    val SATELLITE =
        TileSources.Raster.Default(
            id = TileSourceId.SATELLITE_RASTER_SOURCE_ID,
            url = arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}.jpg"),
            minZoom = 0F,
            maxZoom = 18F,
        )

    val MAPNIK =
        TileSources.Raster.Default(
            id = TileSourceId.MAPNIK_RASTER_SOURCE_ID,
            url = arrayOf("https://tile.openstreetmap.org/{z}/{x}/{y}.png"),
            minZoom = 0F,
            maxZoom = 19F,
        )

    val OPEN_TOPO =
        TileSources.Raster.Default(
            id = TileSourceId.OPEN_TOPO_MAP_SOURCE_ID,
            url = arrayOf("https://tile.opentopomap.org/{z}/{x}/{y}.png"),
            minZoom = 3F,
            maxZoom = 18F,
        )

    val OPEN_SEA_MAP =
        TileSources.Raster.Default(
            id = TileSourceId.OPEN_SEA_MAP_RASTER_SOURCE_ID,
            url =
            arrayOf(
                "https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png",
                "https://t1.openseamap.org/seamark/{z}/{x}/{y}.png",
            ),
            minZoom = 3F,
            maxZoom = 18F,
        )
}