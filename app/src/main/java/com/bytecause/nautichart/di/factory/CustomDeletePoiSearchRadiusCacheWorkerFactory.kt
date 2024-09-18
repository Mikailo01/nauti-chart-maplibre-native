package com.bytecause.nautichart.di.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bytecause.domain.abstractions.RadiusPoiMetadataDatasetRepository
import com.bytecause.nautichart.worker.DeletePoiSearchRadiusCacheWorker
import javax.inject.Inject

class CustomDeletePoiSearchRadiusCacheWorkerFactory @Inject constructor(
    private val radiusPoiMetadataDatasetRepository: RadiusPoiMetadataDatasetRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = DeletePoiSearchRadiusCacheWorker(
        appContext = appContext,
        workParams = workerParameters,
        radiusPoiMetadataDatasetRepository = radiusPoiMetadataDatasetRepository
    )
}