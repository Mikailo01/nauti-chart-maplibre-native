package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOfflineVectorTileSourceRepository {
    suspend fun saveOfflineVectorTileSourceProvider(tileProvider: CustomTileProvider)
    suspend fun deleteOfflineVectorTileSourceProvider(index: Int)
    fun getOfflineVectorTileSourceProviders(): Flow<List<CustomTileProvider>>
}