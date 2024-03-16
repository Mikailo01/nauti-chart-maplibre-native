package com.bytecause.nautichart.data.local.datastore.preferences

import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint

interface UserPreferencesRepositoryInterface {

    suspend fun saveFirstRunFlag(flag: Boolean)
    fun getFirstRunFlag(): Flow<Boolean?>

    suspend fun saveUserPosition(position: GeoPoint)
    fun getUserPosition(): Flow<GeoPoint?>

    suspend fun cacheSelectedTileSource(tileSourceName: String)
    fun getCachedTileSource(): Flow<String?>
}