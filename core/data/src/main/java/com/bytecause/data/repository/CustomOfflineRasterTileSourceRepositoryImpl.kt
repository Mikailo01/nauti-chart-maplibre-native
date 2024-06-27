package com.bytecause.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.datastore.proto.serializer.CustomOfflineRasterTileSourceSerializer
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.nautichart.CustomOfflineRasterTileSource
import com.bytecause.nautichart.CustomOfflineRasterTileSourceList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.customOfflineRasterTileSourceDataStore: DataStore<CustomOfflineRasterTileSourceList> by dataStore(
    fileName = "custom_offline_raster_tile_source_datastore",
    serializer = CustomOfflineRasterTileSourceSerializer
)

class CustomOfflineRasterTileSourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CustomOfflineRasterTileSourceRepository {

    override suspend fun saveOfflineRasterTileSourceProvider(tileProvider: CustomTileProvider) {
        (tileProvider.type as? CustomTileProviderType.Raster.Offline)?.let { provider ->
            withContext(coroutineDispatcher) {
                context.customOfflineRasterTileSourceDataStore.updateData {
                    it.toBuilder().addOfflineRasterTileSource(
                        provider.run {
                            CustomOfflineRasterTileSource.newBuilder()
                                .setName(name)
                                .setMinZoom(minZoom)
                                .setMaxZoom(maxZoom)
                                .setTileSize(tileSize)
                                .setFilePath(filePath)
                        }
                    ).build()
                }
        }
    }
}

override suspend fun deleteOfflineRasterTileSourceProvider(index: Int) {
    withContext(coroutineDispatcher) {
        context.customOfflineRasterTileSourceDataStore.updateData {
            it.toBuilder().removeOfflineRasterTileSource(index).build()
        }
    }
}

override fun getOfflineRasterTileSourceProviders(): Flow<List<CustomTileProvider>> =
    context.customOfflineRasterTileSourceDataStore.data.map {
        it.offlineRasterTileSourceOrBuilderList.map { tileSource ->
            CustomTileProvider(
                CustomTileProviderType.Raster.Offline(
                    name = tileSource.name,
                    minZoom = tileSource.minZoom,
                    maxZoom = tileSource.maxZoom,
                    tileSize = tileSource.tileSize,
                    filePath = tileSource.filePath
                )
            )
        }
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(
                    "CustomOfflineTileSource",
                    "Error reading custom offline tile source provider.",
                    exception
                )
                emit(listOf(CustomTileProvider(CustomTileProviderType.Raster.Offline())))
            } else {
                throw exception
            }
        }
}