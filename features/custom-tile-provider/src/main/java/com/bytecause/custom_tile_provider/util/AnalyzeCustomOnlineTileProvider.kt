package com.bytecause.custom_tile_provider.util

data class TileUrlInfo(val url: String, val tileFileFormat: String)

object AnalyzeCustomOnlineTileProvider {

    fun extractTileUrlAttrs(url: String): TileUrlInfo? {
        if (!checkIsValid(url)) return null

        val tileType: String = when {
            url.endsWith(".jpg", ignoreCase = true) -> ".jpg"
            url.endsWith(".png", ignoreCase = true) -> ".png"
            url.endsWith(".jpeg", ignoreCase = true) -> ".jpeg"
            else -> ""
        }

        return TileUrlInfo(url = url, tileFileFormat = tileType)
    }

    private fun checkIsValid(url: String): Boolean {
        if (!url.contains("https://")) return false
        if (!url.contains("{z}", ignoreCase = true)) return false
        if (!url.contains("{x}", ignoreCase = true)) return false
        if (!url.contains("{y}", ignoreCase = true)) return false

        return true
    }
}