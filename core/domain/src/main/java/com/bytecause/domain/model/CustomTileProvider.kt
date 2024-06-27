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
            val image: ByteArray? = null
        ) : Raster() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Online

                if (name != other.name) return false
                if (url != other.url) return false
                if (tileFileFormat != other.tileFileFormat) return false
                if (minZoom != other.minZoom) return false
                if (maxZoom != other.maxZoom) return false
                if (tileSize != other.tileSize) return false
                if (!image.contentEquals(other.image)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + url.hashCode()
                result = 31 * result + tileFileFormat.hashCode()
                result = 31 * result + minZoom
                result = 31 * result + maxZoom
                result = 31 * result + tileSize
                result = 31 * result + image.contentHashCode()
                return result
            }
        }

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