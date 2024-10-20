package com.bytecause.nautichart.di.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import com.bytecause.nautichart.worker.AnchorageHistoryRemovalWorker
import javax.inject.Inject

class CustomAnchorageHistoryRemovalWorkerFactory @Inject constructor(
    private val anchorageAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository,
    private val anchorageHistoryRepository: AnchorageHistoryRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = AnchorageHistoryRemovalWorker(
        appContext = appContext,
        workParams = workerParameters,
        anchorageAlarmPreferencesRepository = anchorageAlarmPreferencesRepository,
        anchorageHistoryRepository = anchorageHistoryRepository
    )
}