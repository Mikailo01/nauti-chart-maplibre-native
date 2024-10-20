package com.bytecause.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bytecause.data.model.AnchorageHistoryDeletionIntervalModel
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnchorageAlarmPreferencesRepositoryImpl @Inject constructor(
    private val anchorageAlarmPreferencesDataStore: DataStore<Preferences>,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnchorageAlarmPreferencesRepository {

    override suspend fun saveAnchorageLocationsVisible(boolean: Boolean) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[ANCHORAGE_LOCATIONS_VISIBLE] = boolean
            }
        }
    }

    override fun getAnchorageLocationsVisible(): Flow<Boolean> =
        anchorageAlarmPreferencesDataStore.data
            .map { preferences ->
                preferences[ANCHORAGE_LOCATIONS_VISIBLE] ?: false
            }
            .catch { e ->
                if (e is IOException) emit(false)
                else throw e
            }
            .flowOn(coroutineDispatcher)

    override suspend fun saveTrackMovementState(boolean: Boolean) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[TRACK_MOVEMENT_STATE] = boolean
            }
        }
    }

    override fun getTrackMovementState(): Flow<Boolean> = anchorageAlarmPreferencesDataStore.data
        .map { preferences ->
            preferences[TRACK_MOVEMENT_STATE] ?: true
        }
        .catch { e ->
            if (e is IOException) emit(true)
            else throw e
        }
        .flowOn(coroutineDispatcher)

    override suspend fun saveTrackBatteryState(boolean: Boolean) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[TRACK_BATTERY_STATE] = boolean
            }
        }
    }

    override fun getTrackBatteryState(): Flow<Boolean> = anchorageAlarmPreferencesDataStore.data
        .map { preferences ->
            preferences[TRACK_BATTERY_STATE] ?: false
        }
        .catch { e ->
            if (e is IOException) emit(false)
            else throw e
        }
        .flowOn(coroutineDispatcher)

    override suspend fun saveMaxUpdateInterval(interval: Long) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[MAX_UPDATE_INTERVAL] = interval
            }
        }
    }

    override fun getMaxUpdateInterval(): Flow<Long> = anchorageAlarmPreferencesDataStore.data
        .map { preferences ->
            preferences[MAX_UPDATE_INTERVAL] ?: DEFAULT_MAX_INTERVAL
        }
        .catch { e ->
            if (e is IOException) emit(DEFAULT_MAX_INTERVAL)
            else throw e
        }
        .flowOn(coroutineDispatcher)

    override suspend fun saveMinUpdateInterval(interval: Long) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[MIN_UPDATE_INTERVAL] = interval
            }
        }
    }

    override fun getMinUpdateInterval(): Flow<Long> = anchorageAlarmPreferencesDataStore.data
        .map { preferences ->
            preferences[MIN_UPDATE_INTERVAL] ?: DEFAULT_MIN_INTERVAL
        }
        .catch { e ->
            if (e is IOException) emit(DEFAULT_MIN_INTERVAL)
            else throw e
        }
        .flowOn(coroutineDispatcher)

    override suspend fun saveAlarmDelay(delay: Long) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit {
                it[ALARM_DELAY] = delay
            }
        }
    }

    override fun getAlarmDelay(): Flow<Long> = anchorageAlarmPreferencesDataStore.data
        .map { preferences ->
            preferences[ALARM_DELAY] ?: DEFAULT_ALARM_DELAY
        }
        .catch { e ->
            if (e is IOException) emit(DEFAULT_ALARM_DELAY)
            else throw e
        }
        .flowOn(coroutineDispatcher)

    override suspend fun saveAnchorageHistoryDeletionInterval(interval: AnchorageHistoryDeletionIntervalModel) {
        withContext(coroutineDispatcher) {
            anchorageAlarmPreferencesDataStore.edit { preferences ->
                preferences[ANCHORAGE_HISTORY_DELETION_INTERVAL] = interval.name
            }
        }
    }

    override fun getAnchorageHistoryDeletionInterval(): Flow<AnchorageHistoryDeletionIntervalModel> =
        anchorageAlarmPreferencesDataStore.data
            .map { preferences ->
                preferences[ANCHORAGE_HISTORY_DELETION_INTERVAL]?.let {
                    AnchorageHistoryDeletionIntervalModel.valueOf(it)
                } ?: AnchorageHistoryDeletionIntervalModel.TWO_WEEKS
            }
            .catch { e ->
                if (e is IOException) emit(AnchorageHistoryDeletionIntervalModel.TWO_WEEKS)
                else throw e
            }
            .flowOn(coroutineDispatcher)

    companion object {
        private val ANCHORAGE_LOCATIONS_VISIBLE =
            booleanPreferencesKey("anchorage_locations_visible")
        private val MAX_UPDATE_INTERVAL = longPreferencesKey("max_update_interval")
        private val MIN_UPDATE_INTERVAL = longPreferencesKey("min_update_interval")
        private val ALARM_DELAY = longPreferencesKey("alarm_delay")

        private val TRACK_MOVEMENT_STATE = booleanPreferencesKey("track_movement_state")
        private val TRACK_BATTERY_STATE = booleanPreferencesKey("track_battery_state")

        private val ANCHORAGE_HISTORY_DELETION_INTERVAL =
            stringPreferencesKey("anchorage_history_deletion_interval")

        private const val DEFAULT_MAX_INTERVAL = 5000L
        private const val DEFAULT_MIN_INTERVAL = 2000L
        private const val DEFAULT_ALARM_DELAY = 2000L
    }
}