package com.bytecause.domain.tilesources


enum class TileSourceId {
    SATELLITE_RASTER_SOURCE_ID,
    OPEN_SEA_MAP_RASTER_SOURCE_ID,
    MAPNIK_RASTER_SOURCE_ID,
    OPEN_TOPO_MAP_SOURCE_ID,
}

sealed interface TileSources {

    sealed class Raster : TileSources {
        data class Default(
            val id: TileSourceId,
            val url: Array<String>,
            val tileSize: Int = 256,
            val minZoom: Float = 0F,
            val maxZoom: Float = 18F,
        ) : Raster() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Default

                if (id != other.id) return false
                if (!url.contentEquals(other.url)) return false
                if (tileSize != other.tileSize) return false
                if (minZoom != other.minZoom) return false
                if (maxZoom != other.maxZoom) return false

                return true
            }

            override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + url.contentHashCode()
                result = 31 * result + tileSize
                result = 31 * result + minZoom.hashCode()
                result = 31 * result + maxZoom.hashCode()
                return result
            }
        }

        sealed class Custom : Raster() {

            data class Online(
                val id: String,
                val url: String,
                val tileSize: Int = 256,
                val minZoom: Float = 0F,
                val maxZoom: Float = 18F,
            ) : Custom()

            data class Offline(
                val id: String,
                val tileSize: Int = 256,
                val minZoom: Float = 0F,
                val maxZoom: Float = 18F,
                val filePath: String = ""
            ) : Custom()
        }
    }

    sealed class Vector : TileSources {

        sealed class Custom : Vector() {

            data class Offline(
                val id: String,
                val minZoom: Float = 0F,
                val maxZoom: Float = 18F,
                val filePath: String = ""
            ) : Custom()
        }
    }
}