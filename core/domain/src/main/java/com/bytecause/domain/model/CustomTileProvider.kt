package com.bytecause.domain.model


data class CustomTileProvider(
    val type: CustomTileProviderType
)

sealed interface CustomTileProviderType {

    sealed class Raster : CustomTileProviderType {
        data class Online(
            val name: String = "",
            val url: String = "",
            val tileFileFormat: String = "",
            val minZoom: Int = 0,
            val maxZoom: Int = 0,
            val tileSize: Int = 256,
            val imageUrl: String? = null
        ) : Raster()

        data class Offline(
            val name: String = "",
            val minZoom: Int = 0,
            val maxZoom: Int = 0,
            val tileSize: Int = 256,
            val filePath: String = ""
        ) : Raster()
    }

    sealed class Vector : CustomTileProviderType {
        data class Offline(
            val name: String = "",
            val minZoom: Int = 0,
            val maxZoom: Int = 0,
            val filePath: String = ""
        ) : Vector()
    }
}