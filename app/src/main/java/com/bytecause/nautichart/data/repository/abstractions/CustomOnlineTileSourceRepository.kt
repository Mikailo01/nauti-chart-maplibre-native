package com.bytecause.nautichart.data.repository.abstractions

import com.bytecause.nautichart.CustomOnlineTileSource
import com.bytecause.nautichart.CustomOnlineTileSourceList
import kotlinx.coroutines.flow.Flow

interface CustomOnlineTileSourceRepository {

    suspend fun saveOnlineTileSourceProvider(tileProvider: CustomOnlineTileSource)
    suspend fun deleteOnlineTileSourceProvider(index: Int)
    fun getOnlineTileSourceProviders(): Flow<CustomOnlineTileSourceList>
}