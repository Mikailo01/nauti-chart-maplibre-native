package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOnlineRasterTileSourceRepository {

    suspend fun saveOnlineRasterTileSourceProvider(tileProvider: CustomTileProvider)
    suspend fun deleteOnlineRasterTileSourceProvider(index: Int)
    fun getOnlineRasterTileSourceProviders(): Flow<List<CustomTileProvider>>
}