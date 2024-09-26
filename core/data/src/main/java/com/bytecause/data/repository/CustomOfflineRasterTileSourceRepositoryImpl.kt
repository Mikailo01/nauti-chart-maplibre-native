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
import com.bytecause.util.file.FileUtil.offlineTilesDir
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
import java.io.File
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
            it.offlineRasterTileSourceOrBuilderList.mapIndexedNotNull { index, tileSource ->
                if (tilesExist(tileSource.name)) {
                    CustomTileProvider(
                        CustomTileProviderType.Raster.Offline(
                            name = tileSource.name,
                            minZoom = tileSource.minZoom,
                            maxZoom = tileSource.maxZoom,
                            tileSize = tileSource.tileSize,
                            filePath = tileSource.filePath
                        )
                    )
                } else {
                    deleteOfflineRasterTileSourceProvider(index)
                    null
                }
            }
        }
            .catch { exception ->
                exception.printStackTrace()
                if (exception is IOException) emit(listOf(CustomTileProvider(CustomTileProviderType.Raster.Offline())))
                else throw exception
            }
            .flowOn(coroutineDispatcher)

    private fun tilesExist(tilesName: String): Boolean {
        val file = File(context.offlineTilesDir())
        return file.listFiles()?.any { it.name == tilesName } == true
    }
}