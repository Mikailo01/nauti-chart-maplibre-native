package com.bytecause.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.datastore.proto.serializer.CustomOfflineVectorTileSourceSerializer
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.nautichart.CustomOfflineVectorTileSource
import com.bytecause.nautichart.CustomOfflineVectorTileSourceList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.customOfflineVectorTileSourceDataStore: DataStore<CustomOfflineVectorTileSourceList> by dataStore(
    fileName = "custom_offline_vector_tile_source_datastore",
    serializer = CustomOfflineVectorTileSourceSerializer
)

class CustomOfflineVectorTileSourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CustomOfflineVectorTileSourceRepository {

    override suspend fun saveOfflineVectorTileSourceProvider(tileProvider: CustomTileProvider) {
        (tileProvider.type as? CustomTileProviderType.Vector.Offline)?.let { provider ->
            withContext(coroutineDispatcher) {
                context.customOfflineVectorTileSourceDataStore.updateData {
                    it.toBuilder().addOfflineVectorTileSource(
                        provider.run {
                            CustomOfflineVectorTileSource.newBuilder()
                                .setName(name)
                                .setMinZoom(minZoom)
                                .setMaxZoom(maxZoom)
                                .setFilePath(filePath)
                        }
                    ).build()
                }
            }
        }
    }

    override suspend fun deleteOfflineVectorTileSourceProvider(index: Int) {
        withContext(coroutineDispatcher) {
            context.customOfflineVectorTileSourceDataStore.updateData {
                it.toBuilder().removeOfflineVectorTileSource(index).build()
            }
        }
    }

    override fun getOfflineVectorTileSourceProviders(): Flow<List<CustomTileProvider>> =
        context.customOfflineVectorTileSourceDataStore.data.map {
            it.offlineVectorTileSourceOrBuilderList.map { tileSource ->
                CustomTileProvider(
                    CustomTileProviderType.Vector.Offline(
                        name = tileSource.name,
                        minZoom = tileSource.minZoom,
                        maxZoom = tileSource.maxZoom,
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
                    emit(listOf(CustomTileProvider(CustomTileProviderType.Vector.Offline())))
                } else {
                    throw exception
                }
            }
}