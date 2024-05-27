package com.bytecause.nautichart.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.nautichart.CustomOfflineTileSource
import com.bytecause.nautichart.CustomOfflineTileSourceList
import com.bytecause.nautichart.CustomOnlineTileSourceList
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.nautichart.data.local.datastore.proto.serializers.CustomOfflineTileSourceSerializer
import com.bytecause.nautichart.data.local.datastore.proto.serializers.RecentlySearchedPlaceSerializer
import com.bytecause.nautichart.data.repository.abstractions.CustomOfflineTileSourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.customOfflineTileSourceDataStore: DataStore<CustomOfflineTileSourceList> by dataStore(
    fileName = "custom_offline_tile_source_datastore",
    serializer = CustomOfflineTileSourceSerializer
)

class CustomOfflineTileSourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CustomOfflineTileSourceRepository {
    override suspend fun saveOfflineTileSourceProvider(tileProvider: CustomOfflineTileSource) {
        withContext(coroutineDispatcher) {
            context.customOfflineTileSourceDataStore.updateData {
                it.toBuilder().addOfflineTileSource(tileProvider).build()
            }
        }
    }

    override suspend fun deleteOfflineTileSourceProvider(index: Int) {
        withContext(coroutineDispatcher) {
            context.customOfflineTileSourceDataStore.updateData {
                it.toBuilder().removeOfflineTileSource(index).build()
            }
        }
    }

    override fun getOfflineTileSourceProviders(): Flow<CustomOfflineTileSourceList> =
        context.customOfflineTileSourceDataStore.data
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    Log.e(
                        "CustomOfflineTileSource",
                        "Error reading custom offline tile source provider.",
                        exception
                    )
                    emit(CustomOfflineTileSourceList.getDefaultInstance())
                } else {
                    throw exception
                }
            }
}