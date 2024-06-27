package com.bytecause.download_tiles

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

object CustomTileSourceFactory : TileSourceFactory() {

    val OPEN_SEAMAP: OnlineTileSourceBase = XYTileSource(
        "OpenSeaMapSeaMarks",
        3,
        18,
        256,
        ".png",
        arrayOf(
            "https://tiles.openseamap.org/seamark/",
            "https://t1.openseamap.org/seamark/"
        ),
        "OpenSeaMap"
    )

    val SAT: OnlineTileSourceBase = YXTileSource(
        "Satellite",
        0,
        18,
        256,
        "",
        arrayOf("https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/"),
        "© ArcGIS | © Esri | © OpenSeaMap"
    )
}

class YXTileSource(
    aName: String, minZoom: Int,
    maxZoom: Int, tileSize: Int,
    filenameEnding: String, url: Array<String>, copyright: String
) : XYTileSource(aName, minZoom, maxZoom, tileSize, filenameEnding, url, copyright) {

    override fun getTileURLString(pMapTileIndex: Long): String {
        return (baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getY(
            pMapTileIndex
        ) + "/" + MapTileIndex.getX(pMapTileIndex)
                + mImageFilenameEnding)
    }
}