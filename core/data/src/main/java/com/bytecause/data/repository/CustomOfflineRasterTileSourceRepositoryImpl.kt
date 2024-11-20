package com.bytecause.data.repository

import android.content.Context
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
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
        withContext(coroutineDispatcher) {
            (tileProvider.type as? CustomTileProviderType.Raster.Offline)?.let { provider ->

                // Delete any existing tile source with the same name if present
                context.customOfflineRasterTileSourceDataStore.data.firstOrNull()?.let {
                    it.offlineRasterTileSourceOrBuilderList.forEachIndexed { index, tileSource ->
                        if (tileSource.name == provider.name) {
                            deleteOfflineRasterTileSourceProvider(index).firstOrNull()
                        }
                    }
                }

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

    override fun deleteOfflineRasterTileSourceProvider(index: Int): Flow<String?> = flow {
        val currentData = context.customOfflineRasterTileSourceDataStore.data.firstOrNull()
        val deletedItemName = currentData?.offlineRasterTileSourceList?.getOrNull(index)?.name

        context.customOfflineRasterTileSourceDataStore.updateData {
            it.toBuilder().removeOfflineRasterTileSource(index).build()
        }

        emit(deletedItemName)
    }
        .catch { exception ->
            exception.printStackTrace()
            if (exception is IOException) emit(null)
            else throw exception
        }
        .flowOn(coroutineDispatcher)

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
            .catch { exception ->
                exception.printStackTrace()
                if (exception is IOException) emit(listOf(CustomTileProvider(CustomTileProviderType.Raster.Offline())))
                else throw exception
            }
            .flowOn(coroutineDispatcher)
}