package com.bytecause.domain.abstractions

import com.bytecause.domain.model.CustomTileProvider
import kotlinx.coroutines.flow.Flow

interface CustomOnlineRasterTileSourceRepository {

    suspend fun saveOnlineRasterTileSourceProvider(tileProvider: CustomTileProvider)
    /**
     * @return Name of deleted provider
     * **/
    fun deleteOnlineRasterTileSourceProvider(index: Int): Flow<String?>
    fun getOnlineRasterTileSourceProviders(): Flow<List<CustomTileProvider>>
}