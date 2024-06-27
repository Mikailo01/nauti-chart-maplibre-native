package com.bytecause.data.repository.abstractions

import kotlinx.coroutines.flow.Flow
import org.maplibre.android.geometry.LatLng

interface UserPreferencesRepository {

    suspend fun saveFirstRunFlag(flag: Boolean)
    fun getFirstRunFlag(): Flow<Boolean?>

    suspend fun saveUserPosition(position: LatLng)
    fun getUserPosition(): Flow<LatLng?>

    suspend fun cacheSelectedTileSource(tileSourceName: String)
    fun getCachedTileSource(): Flow<String?>

    suspend fun cacheLoadedStyle(styleName: String)
    fun getCachedStyle(): Flow<String?>
}