package com.bytecause.custom_tile_provider.data.repository.abstractions

interface GetTileImageRepository {
    suspend fun getImage(url: String): ByteArray?
}