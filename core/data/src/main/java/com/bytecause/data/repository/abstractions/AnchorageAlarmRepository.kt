package com.bytecause.data.repository.abstractions

import com.bytecause.domain.model.RunningAnchorageAlarmModel
import kotlinx.coroutines.flow.Flow

interface AnchorageAlarmRepository {
    suspend fun saveRunningAnchorageAlarm(alarm: RunningAnchorageAlarmModel)
    suspend fun deleteRunningAnchorageAlarm()
    fun getRunningAnchorageAlarm(): Flow<RunningAnchorageAlarmModel>
}