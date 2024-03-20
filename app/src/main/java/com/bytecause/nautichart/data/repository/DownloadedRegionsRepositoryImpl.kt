package com.bytecause.nautichart.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.bytecause.nautichart.data.local.datastore.preferences.DownloadedRegionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DownloadedRegionsRepositoryImpl @Inject constructor(
    private val regionDatastore: DataStore<Preferences>
) : DownloadedRegionsRepository {

    override fun getDownloadedRegionsIds(): Flow<Set<String>> = flow {
        emit(regionDatastore.data.firstOrNull()?.get(DOWNLOADED_REGIONS_KEY) ?: emptySet())
    }

    override suspend fun addDownloadedRegion(regionId: String) {
        withContext(Dispatchers.IO) {
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