package com.bytecause.custom_tile_provider.util

object Util {
    fun formatTileUrlForRaster(url: String, z: Int = 4, y: Int = 5, x: Int = 8): String {
        return url
            .replace("{z}", z.toString())
            .replace("{y}", y.toString())
            .replace("{x}", x.toString())
    }
}