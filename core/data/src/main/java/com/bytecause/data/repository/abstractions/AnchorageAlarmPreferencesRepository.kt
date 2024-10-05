package com.bytecause.data.repository.abstractions

import kotlinx.coroutines.flow.Flow

interface AnchorageAlarmPreferencesRepository {
    suspend fun saveMaxUpdateInterval(interval: Long)
    fun getMaxUpdateInterval(): Flow<Long>

    suspend fun saveMinUpdateInterval(interval: Long)
    fun getMinUpdateInterval(): Flow<Long>

    suspend fun saveAlarmDelay(delay: Long)
    fun getAlarmDelay(): Flow<Long>

    suspend fun saveAnchorageLocationsVisible(boolean: Boolean)
    fun getAnchorageLocationsVisible(): Flow<Boolean>
}