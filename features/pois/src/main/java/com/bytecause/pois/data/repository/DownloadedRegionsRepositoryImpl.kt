package com.bytecause.pois.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.bytecause.data.di.IoDispatcher
import com.bytecause.pois.data.repository.abstractions.DownloadedRegionsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DownloadedRegionsRepositoryImpl @Inject constructor(
    private val regionDatastore: DataStore<Preferences>,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DownloadedRegionsRepository {

    override fun getDownloadedRegionsIds(): Flow<Set<String>> = flow {
        emit(regionDatastore.data.firstOrNull()?.get(DOWNLOADED_REGIONS_KEY) ?: emptySet())
    }
        .flowOn(coroutineDispatcher)

    override suspend fun addDownloadedRegion(regionId: String) {
        withContext(coroutineDispatcher) {
            regionDatastore.edit { preferences ->
                val currentList = preferences[DOWNLOADED_REGIONS_KEY] ?: emptySet()
                preferences[DOWNLOADED_REGIONS_KEY] = currentList + regionId
            }
        }
    }

    private companion object {
        private val DOWNLOADED_REGIONS_KEY = stringSetPreferencesKey("downloaded_regions")
    }
}