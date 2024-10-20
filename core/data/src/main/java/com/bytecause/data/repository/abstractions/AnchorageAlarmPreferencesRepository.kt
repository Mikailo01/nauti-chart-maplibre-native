package com.bytecause.data.repository.abstractions

import com.bytecause.data.model.AnchorageHistoryDeletionIntervalModel
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

    suspend fun saveTrackMovementState(boolean: Boolean)
    fun getTrackMovementState(): Flow<Boolean>

    suspend fun saveTrackBatteryState(boolean: Boolean)
    fun getTrackBatteryState(): Flow<Boolean>

    suspend fun saveAnchorageHistoryDeletionInterval(interval: AnchorageHistoryDeletionIntervalModel)
    fun getAnchorageHistoryDeletionInterval(): Flow<AnchorageHistoryDeletionIntervalModel>
}