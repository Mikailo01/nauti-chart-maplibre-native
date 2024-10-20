package com.bytecause.nautichart.di.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bytecause.nautichart.worker.AnchorageHistoryRemovalWorker
import com.bytecause.nautichart.worker.UpdateExpiredDatasetsWorker
import com.bytecause.nautichart.worker.DeletePoiSearchRadiusCacheWorker
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val updateExpiredDatasetsWorkerFactory: CustomUpdateExpiredDatasetsWorkerFactory,
    private val deletePoiSearchRadiusCacheWorkerFactory: CustomDeletePoiSearchRadiusCacheWorkerFactory,
    private val anchorageHistoryRemovalWorkerFactory: CustomAnchorageHistoryRemovalWorkerFactory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            UpdateExpiredDatasetsWorker::class.java.name -> {
                updateExpiredDatasetsWorkerFactory.createWorker(
                    appContext = appContext,
                    workerClassName = workerClassName,
                    workerParameters = workerParameters
                )
            }

            DeletePoiSearchRadiusCacheWorker::class.java.name -> {
                deletePoiSearchRadiusCacheWorkerFactory.createWorker(
                    appContext = appContext,
                    workerClassName = workerClassName,
                    workerParameters = workerParameters
                )
            }

            AnchorageHistoryRemovalWorker::class.java.name -> {
                anchorageHistoryRemovalWorkerFactory.createWorker(
                    appContext = appContext,
                    workerClassName = workerClassName,
                    workerParameters = workerParameters
                )
            }

            else -> {
                null // Fallback if no matching factory is found
            }
        }
    }
}
