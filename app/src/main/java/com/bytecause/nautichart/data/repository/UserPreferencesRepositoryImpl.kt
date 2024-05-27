package com.bytecause.nautichart.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bytecause.nautichart.data.local.datastore.preferences.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userDataStorePreferences: DataStore<Preferences>,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
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
        else throw exception
    }

    override suspend fun saveUserPosition(position: GeoPoint) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[USER_POSITION_KEY] = Gson().toJson(position)
            }
        }
    }

    override fun getUserPosition(): Flow<GeoPoint?> = flow {
        val jsonString =
            userDataStorePreferences.data.firstOrNull()?.get(USER_POSITION_KEY)
        emit(Gson().fromJson(jsonString, GeoPoint::class.java))
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
        if (exception is IOException) emit(null)
        else throw exception
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
        else throw exception
    }


    private companion object {
        val USER_POSITION_KEY = stringPreferencesKey("user_position")
        val TILE_SOURCE_KEY = stringPreferencesKey("tile_source")
        val FIRST_RUN_KEY = booleanPreferencesKey("first_run")
    }
}