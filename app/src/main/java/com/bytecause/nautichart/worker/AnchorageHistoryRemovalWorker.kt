package com.bytecause.nautichart.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class AnchorageHistoryRemovalWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workParams: WorkerParameters,
    private val anchorageAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository,
    private val anchorageHistoryRepository: AnchorageHistoryRepository
) : CoroutineWorker(appContext, workParams) {

    override suspend fun doWork(): Result {
        val interval =
            anchorageAlarmPreferencesRepository.getAnchorageHistoryDeletionInterval().firstOrNull()
        val history =
            anchorageHistoryRepository.getAnchorageHistoryList().firstOrNull()?.anchorageHistoryList

        if (interval == null || interval.interval == -1L) return Result.success()
        if (history.isNullOrEmpty()) return Result.success()

        val filteredHistory =
            history.filter { (it.timestamp + interval.interval) <= System.currentTimeMillis() }

        filteredHistory.forEach {
            anchorageHistoryRepository.removeAnchorageHistory(it.id)
        }

        return Result.success()
    }
}