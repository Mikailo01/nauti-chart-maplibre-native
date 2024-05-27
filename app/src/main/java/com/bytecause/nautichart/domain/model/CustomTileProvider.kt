package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.ui.view.fragment.dialog.TileFileFormat

/*data class CustomTileProvider(
    val name: String = "",
    val url: String? = null,
    val tileFileFormat: String = "",
    val schema: String? = null,
    val minZoom: Int = 0,
    val maxZoom: Int = 0,
    val tileSize: Int = 256
)*/

data class CustomTileProvider(
    val type: CustomTileProviderType
)

sealed interface CustomTileProviderType {
    data class Online(
        val name: String = "",
        val url: String = "",
        val tileFileFormat: String = "",
        val schema: String = "",
        val minZoom: Int = 0,
        val maxZoom: Int = 0,
        val tileSize: Int = 256
    ) : CustomTileProviderType
    data class Offline(
        val name: String = "",
        val minZoom: Int = 0,
        val maxZoom: Int = 0,
        val tileSize: Int = 256
    ) : CustomTileProviderType
}