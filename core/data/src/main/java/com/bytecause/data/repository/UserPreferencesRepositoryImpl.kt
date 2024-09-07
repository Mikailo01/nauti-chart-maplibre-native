package com.bytecause.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.bytecause.data.di.IoDispatcher
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.util.GsonProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userDataStorePreferences: DataStore<Preferences>,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    override suspend fun saveFirstRunFlag(flag: Boolean) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[FIRST_RUN_KEY] = flag
            }
        }
    }

    override fun getFirstRunFlag(): Flow<Boolean?> = flow {
        val firstRunFlag = userDataStorePreferences.data.firstOrNull()?.get(FIRST_RUN_KEY)
        emit(firstRunFlag)
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            throw exception
        }

    override suspend fun saveUserPosition(position: LatLngModel) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[USER_POSITION_KEY] = GsonProvider.gson.toJson(position)
            }
        }
    }

    override fun getUserPosition(): Flow<LatLngModel?> = flow {
        val jsonString =
            userDataStorePreferences.data.firstOrNull()?.get(USER_POSITION_KEY)
        emit(GsonProvider.gson.fromJson(jsonString, LatLngModel::class.java))
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            throw exception
        }

    override suspend fun cacheSelectedTileSource(tileSourceName: String) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[TILE_SOURCE_KEY] = tileSourceName
            }
        }
    }

    override fun getCachedTileSource(): Flow<String?> = flow {
        val cachedTileSource = userDataStorePreferences.data.firstOrNull()?.get(TILE_SOURCE_KEY)
        emit(cachedTileSource)
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            throw exception
        }

    override suspend fun cacheLoadedStyle(styleName: String) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[STYLE_KEY] = styleName
            }
        }
    }

    override fun getCachedStyle(): Flow<String?> = flow {
        emit(userDataStorePreferences.data.firstOrNull()?.get(STYLE_KEY))
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            throw exception
        }

    override fun getSelectedPoiCategories(): Flow<Set<String>> {
        return userDataStorePreferences.data
            .map { preferences ->
                preferences[SELECTED_POI_CATEGORIES_KEY] ?: emptySet()
            }
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                if (exception is IOException) emit(emptySet())
                throw exception
            }
    }

    override suspend fun saveIsAisActivated(boolean: Boolean) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[IS_AIS_ACTIVATED] = boolean
            }
        }
    }

    override fun getIsAisActivated(): Flow<Boolean> {
        return userDataStorePreferences.data
            .map { preferences ->
                preferences[IS_AIS_ACTIVATED] ?: false
            }
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                if (exception is IOException) emit(false)
                throw exception
            }
    }

    override suspend fun saveAreHarboursVisible(boolean: Boolean) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[ARE_HARBOURS_VISIBLE] = boolean
            }
        }
    }

    override fun getAreHarboursVisible(): Flow<Boolean> {
        return userDataStorePreferences.data
            .map { preferences ->
                preferences[ARE_HARBOURS_VISIBLE] ?: false
            }
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                if (exception is IOException) emit(false)
                throw exception
            }
    }

    override suspend fun savePoiUpdateInterval(interval: Long) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[POI_UPDATE_INTERVAL] = interval
            }
        }
    }

    override fun getPoiUpdateInterval(): Flow<Long> =
        userDataStorePreferences.data
            .map { preferences ->
                // 2 weeks interval as default value
                preferences[POI_UPDATE_INTERVAL] ?: 1_209_600_000L
            }
            .catch { exception ->
                if (exception is IOException) emit(1_209_600_000L)
                throw exception
            }
            .flowOn(coroutineDispatcher)

    override suspend fun saveHarboursUpdateInterval(interval: Long) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[HARBOURS_UPDATE_INTERVAL] = interval
            }
        }
    }

    override fun getHarboursUpdateInterval(): Flow<Long> =
        userDataStorePreferences.data
            .map { preferences ->
                // 2 weeks interval as default value
                preferences[HARBOURS_UPDATE_INTERVAL] ?: 1_209_600_000L
            }
            .catch { exception ->
                if (exception is IOException) emit(1_209_600_000L)
                throw exception
            }
            .flowOn(coroutineDispatcher)


    override suspend fun saveSelectedPoiCategories(set: Set<String>) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[SELECTED_POI_CATEGORIES_KEY] = set
            }
        }
    }

    private companion object {
        private val USER_POSITION_KEY = stringPreferencesKey("user_position")
        private val TILE_SOURCE_KEY = stringPreferencesKey("tile_source")
        private val STYLE_KEY = stringPreferencesKey("style_key")
        private val FIRST_RUN_KEY = booleanPreferencesKey("first_run")
        private val SELECTED_POI_CATEGORIES_KEY = stringSetPreferencesKey("selected_poi_categories")
        private val IS_AIS_ACTIVATED = booleanPreferencesKey("is_ais_activated")
        private val ARE_HARBOURS_VISIBLE = booleanPreferencesKey("are_harbours_visible")
        private val POI_UPDATE_INTERVAL = longPreferencesKey("poi_update_interval")
        private val HARBOURS_UPDATE_INTERVAL = longPreferencesKey("harbours_update_interval")
    }
}