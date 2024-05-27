package com.bytecause.nautichart.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.nautichart.CustomOnlineTileSource
import com.bytecause.nautichart.CustomOnlineTileSourceList
import com.bytecause.nautichart.RecentlySearchedPlaceList
import com.bytecause.nautichart.data.local.datastore.proto.serializers.CustomOnlineTileSourceSerializer
import com.bytecause.nautichart.data.local.datastore.proto.serializers.RecentlySearchedPlaceSerializer
import com.bytecause.nautichart.data.repository.abstractions.CustomOnlineTileSourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.customOnlineTileSourceDataStore: DataStore<CustomOnlineTileSourceList> by dataStore(
    fileName = "custom_online_tile_source_datastore",
    serializer = CustomOnlineTileSourceSerializer
)

class CustomOnlineTileSourceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomOnlineTileSourceRepository {
    override suspend fun saveOnlineTileSourceProvider(tileProvider: CustomOnlineTileSource) {
        withContext(coroutineDispatcher) {
            context.customOnlineTileSourceDataStore.updateData {
                it.toBuilder().addOnlineTileSource(tileProvider).build()
            }
        }
    }

    override suspend fun deleteOnlineTileSourceProvider(index: Int) {
        withContext(coroutineDispatcher) {
            context.customOnlineTileSourceDataStore.updateData {
                it.toBuilder().removeOnlineTileSource(index).build()
            }
        }
    }

    override fun getOnlineTileSourceProviders(): Flow<CustomOnlineTileSourceList> =
        context.customOnlineTileSourceDataStore.data
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    Log.e("CustomOnlineTileSource", "Error reading custom online tile source provider.", exception)
                    emit(CustomOnlineTileSourceList.getDefaultInstance())
                } else {
                    throw exception
                }
            }

}