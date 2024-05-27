package com.bytecause.nautichart.tilesources

data class TileUrlInfo(val baseUrl: String, val tileFileFormat: String?, val schema: String)
enum class TileProviderUrlParametersSchema {
    XY,
    YX
}

object AnalyzeCustomOnlineTileProvider {

    fun extractTileUrlAttrs(url: String): TileUrlInfo? {
        if (!checkIsValid(url)) return null

        val baseUrl = url.substring(0, url.indexOf("{z}", ignoreCase = true))

        val schema = if (url.indexOf("{x}", ignoreCase = true) < url.indexOf(
                "{y}",
                ignoreCase = true
            )
        ) TileProviderUrlParametersSchema.XY.name
        else TileProviderUrlParametersSchema.YX.name

        val tileType: String = when {
            url.endsWith(".jpg", ignoreCase = true) -> ".jpg"
            url.endsWith(".png", ignoreCase = true) -> ".png"
            url.endsWith(".jpeg", ignoreCase = true) -> ".jpeg"
            else -> ""
        }

        return TileUrlInfo(baseUrl = baseUrl, tileFileFormat = tileType, schema = schema)
    }

    private fun checkIsValid(url: String): Boolean {
        if (!url.contains("https://")) return false
        if (!url.contains("{z}", ignoreCase = true)) return false
        if (!url.contains("{x}", ignoreCase = true)) return false
        if (!url.contains("{y}", ignoreCase = true)) return false

        return true
    }
}