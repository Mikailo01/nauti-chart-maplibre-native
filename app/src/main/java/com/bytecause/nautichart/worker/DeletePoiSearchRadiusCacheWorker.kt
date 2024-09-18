package com.bytecause.nautichart.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bytecause.domain.abstractions.RadiusPoiMetadataDatasetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

private const val DATASET_EXPIRATION_THRESHOLD = 604_800_000L // 7 days

@HiltWorker
class DeletePoiSearchRadiusCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workParams: WorkerParameters,
    private val radiusPoiMetadataDatasetRepository: RadiusPoiMetadataDatasetRepository
) : CoroutineWorker(appContext, workParams) {

    override suspend fun doWork(): Result {
        radiusPoiMetadataDatasetRepository.getAllDatasets().firstOrNull()?.let { datasetList ->
            datasetList.onEach { dataset ->
                if ((System.currentTimeMillis() - dataset.timestamp) >= DATASET_EXPIRATION_THRESHOLD) {
                    radiusPoiMetadataDatasetRepository.deleteDataset(dataset.category)
                }
            }
        }

        return Result.success()
    }
}