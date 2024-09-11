package com.bytecause.domain.abstractions

import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.model.NetworkType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    suspend fun saveFirstRunFlag(flag: Boolean)
    fun getFirstRunFlag(): Flow<Boolean?>

    suspend fun saveUserPosition(position: LatLngModel)
    fun getUserPosition(): Flow<LatLngModel?>

    suspend fun cacheSelectedTileSource(tileSourceName: String)
    fun getCachedTileSource(): Flow<String?>

    suspend fun cacheLoadedStyle(styleName: String)
    fun getCachedStyle(): Flow<String?>

    suspend fun saveSelectedPoiCategories(set: Set<String>)
    fun getSelectedPoiCategories(): Flow<Set<String>>

    suspend fun saveIsAisActivated(boolean: Boolean)
    fun getIsAisActivated(): Flow<Boolean>

    suspend fun saveAreHarboursVisible(boolean: Boolean)
    fun getAreHarboursVisible(): Flow<Boolean>

    suspend fun savePoiUpdateInterval(interval: Long)
    fun getPoiUpdateInterval(): Flow<Long>

    suspend fun saveHarboursUpdateInterval(interval: Long)
    fun getHarboursUpdateInterval(): Flow<Long>

    suspend fun saveAutoUpdateNetworkPreference(networkType: NetworkType)
    fun getAutoUpdatesNetworkPreference(): Flow<NetworkType>
}