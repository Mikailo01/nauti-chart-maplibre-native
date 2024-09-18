package com.bytecause.nautichart.di.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bytecause.nautichart.worker.UpdateExpiredDatasetsWorker
import com.bytecause.domain.usecase.UpdateExpiredDatasetsUseCase
import javax.inject.Inject

class CustomUpdateExpiredDatasetsWorkerFactory @Inject constructor(
    private val updateExpiredDatasetsUseCase: UpdateExpiredDatasetsUseCase
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = UpdateExpiredDatasetsWorker(
        appContext = appContext,
        workerParams = workerParameters,
        updateExpiredDatasetsUseCase = updateExpiredDatasetsUseCase
    )
}