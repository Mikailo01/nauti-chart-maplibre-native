package com.bytecause.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.datastore.proto.serializer.CustomOnlineRasterTileSourceSerializer
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.nautichart.CustomOnlineRasterTileSource
import com.bytecause.nautichart.CustomOnlineRasterTileSourceList
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

private val Context.customOnlineTileSourceDataStore: DataStore<CustomOnlineRasterTileSourceList> by dataStore(
    fileName = "custom_online_tile_source_datastore",
    serializer = CustomOnlineRasterTileSourceSerializer
)

class CustomOnlineRasterTileSourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomOnlineRasterTileSourceRepository {

    override suspend fun saveOnlineRasterTileSourceProvider(tileProvider: CustomTileProvider) {
        (tileProvider.type as? CustomTileProviderType.Raster.Online)?.let { onlineProvider ->
            withContext(coroutineDispatcher) {
                context.customOnlineTileSourceDataStore.updateData {
                    it.toBuilder().addOnlineRasterTileSource(
                        onlineProvider.run {
                            CustomOnlineRasterTileSource.newBuilder()
                                .setName(name)
                                .setUrl(url)
                                .setTileFileFormat(tileFileFormat)
                                .setMinZoom(minZoom)
                                .setMaxZoom(maxZoom)
                                .setTileSize(tileSize)
                                .setImageUrl(imageUrl)
                        }
                    ).build()
                }
            }
        }
    }

    override fun deleteOnlineRasterTileSourceProvider(index: Int): Flow<String?> = flow {
        val currentData = context.customOnlineTileSourceDataStore.data.firstOrNull()
        val deletedItemName = currentData?.onlineRasterTileSourceList?.getOrNull(index)?.name

        context.customOnlineTileSourceDataStore.updateData {
            it.toBuilder().removeOnlineRasterTileSource(index).build()
        }

        emit(deletedItemName)
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            exception.printStackTrace()
            if (exception is IOException) {
                emit(null)
            } else {
                throw exception
            }
        }

    override fun getOnlineRasterTileSourceProviders(): Flow<List<CustomTileProvider>> =
        context.customOnlineTileSourceDataStore.data.map {
            it.onlineRasterTileSourceOrBuilderList.map { tileSource ->
                CustomTileProvider(
                    CustomTileProviderType.Raster.Online(
                        name = tileSource.name,
                        url = tileSource.url,
                        tileFileFormat = tileSource.tileFileFormat,
                        minZoom = tileSource.minZoom,
                        maxZoom = tileSource.maxZoom,
                        tileSize = tileSource.tileSize,
                        imageUrl = tileSource.imageUrl
                    )
                )
            }
        }
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                exception.printStackTrace()
                if (exception is IOException) {
                    emit(listOf(CustomTileProvider(CustomTileProviderType.Raster.Online())))
                } else {
                    throw exception
                }
            }

}