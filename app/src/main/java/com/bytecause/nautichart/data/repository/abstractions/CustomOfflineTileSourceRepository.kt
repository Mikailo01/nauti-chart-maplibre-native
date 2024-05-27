package com.bytecause.nautichart.data.repository.abstractions

import com.bytecause.nautichart.CustomOfflineTileSource
import com.bytecause.nautichart.CustomOfflineTileSourceList
import kotlinx.coroutines.flow.Flow
import org.oscim.utils.quadtree.TileIndex

interface CustomOfflineTileSourceRepository {
    suspend fun saveOfflineTileSourceProvider(tileProvider: CustomOfflineTileSource)
    suspend fun deleteOfflineTileSourceProvider(index: Int)
    fun getOfflineTileSourceProviders(): Flow<CustomOfflineTileSourceList>
}