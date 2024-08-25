package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOfflineVectorTileSourceRepository {
    suspend fun saveOfflineVectorTileSourceProvider(tileProvider: CustomTileProvider)
    /**
     * @return Name of deleted provider
     * **/
    fun deleteOfflineVectorTileSourceProvider(index: Int): Flow<String?>
    fun getOfflineVectorTileSourceProviders(): Flow<List<CustomTileProvider>>
}