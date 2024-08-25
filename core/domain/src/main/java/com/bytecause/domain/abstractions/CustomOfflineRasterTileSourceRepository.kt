package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOfflineRasterTileSourceRepository {
    suspend fun saveOfflineRasterTileSourceProvider(tileProvider: CustomTileProvider)
    /**
     * @return Name of deleted provider
     * **/
    fun deleteOfflineRasterTileSourceProvider(index: Int): Flow<String?>
    fun getOfflineRasterTileSourceProviders(): Flow<List<CustomTileProvider>>
}