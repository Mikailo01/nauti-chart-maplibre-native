package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOfflineRasterTileSourceRepository {
    suspend fun saveOfflineRasterTileSourceProvider(tileProvider: CustomTileProvider)
    suspend fun deleteOfflineRasterTileSourceProvider(index: Int)
    fun getOfflineRasterTileSourceProviders(): Flow<List<CustomTileProvider>>
}