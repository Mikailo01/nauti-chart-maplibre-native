package com.bytecause.custom_tile_provider.data.repository

import com.bytecause.custom_tile_provider.data.remote.GetTileImageRemoteDataSource
import com.bytecause.custom_tile_provider.data.repository.abstractions.GetTileImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTileImageRepositoryImpl @Inject constructor(
    private val getTileImageDataSource: GetTileImageRemoteDataSource,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GetTileImageRepository {
    override suspend fun getImage(url: String): ByteArray? = withContext(coroutineDispatcher) {
        getTileImageDataSource.downloadImage(formatTileUrl(url))
    }

    private fun formatTileUrl(url: String, z: Int = 4, y: Int = 5, x: Int = 8): String {
        return url
            .replace("{z}", z.toString())
            .replace("{y}", y.toString())
            .replace("{x}", x.toString())
    }

}