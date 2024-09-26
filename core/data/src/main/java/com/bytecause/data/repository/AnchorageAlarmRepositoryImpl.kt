package com.bytecause.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.bytecause.data.local.datastore.proto.serializer.RunningAnchorageAlarmSerializer
import com.bytecause.data.repository.abstractions.AnchorageAlarmRepository
import com.bytecause.domain.model.RunningAnchorageAlarmModel
import com.bytecause.nautichart.RunningAnchorageAlarm
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.runningAnchorageAlarmDataStore: DataStore<RunningAnchorageAlarm> by dataStore(
    fileName = "running_anchorage_alarm",
    serializer = RunningAnchorageAlarmSerializer
)

class AnchorageAlarmRepositoryImpl(
    @ApplicationContext private val context: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnchorageAlarmRepository {
    override suspend fun saveRunningAnchorageAlarm(alarm: RunningAnchorageAlarmModel) {
        withContext(coroutineDispatcher) {
            context.runningAnchorageAlarmDataStore.updateData {
                alarm.run {
                    it.toBuilder()
                        .setIsRunning(isRunning)
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .setRadius(radius)
                        .build()
                }
            }
        }
    }

    override fun getRunningAnchorageAlarm(): Flow<RunningAnchorageAlarmModel> =
        context.runningAnchorageAlarmDataStore.data
            .map {
                RunningAnchorageAlarmModel(
                    isRunning = it.isRunning,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radius = it.radius
                )
            }
            .catch { e ->
                if (e is IOException) emit(RunningAnchorageAlarmModel())
                else throw e
            }
            .flowOn(coroutineDispatcher)

    override suspend fun deleteRunningAnchorageAlarm() {
        withContext(coroutineDispatcher) {
            context.runningAnchorageAlarmDataStore.updateData {
                it.toBuilder().clear().build()
            }
        }
    }
}