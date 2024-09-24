package com.bytecause.nautichart.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bytecause.core.resources.R
import com.bytecause.domain.model.DatasetType
import com.bytecause.domain.model.UpdateResult
import com.bytecause.domain.usecase.UpdateExpiredDatasetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException

private const val NOTIFICATION_ID = 3
private const val CHANNEL_ID = "dataset_update_channel"
private const val INITIAL_PROGRESS = -1

@HiltWorker
class UpdateExpiredDatasetsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateExpiredDatasetsUseCase: UpdateExpiredDatasetsUseCase
) : CoroutineWorker(appContext, workerParams) {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var progress: Int = INITIAL_PROGRESS

    override suspend fun doWork(): Result {

        val resultDeferred = CompletableDeferred<Result>()

        updateExpiredDatasetsUseCase().collect { result ->
            when (result) {
                is UpdateResult.UpdateProgress -> updateNotificationProgress(result.progress)
                is UpdateResult.UpdateStarted -> {
                    if (!::notificationManager.isInitialized) {
                        // Set up the notification
                        setForeground(createForegroundInfo(result.type))
                    } else updateStartedNotificationContentUpdate(result.type)
                }

                is UpdateResult.UpdateFailed -> {
                    if (result.error is IOException) {
                        resultDeferred.complete(Result.retry())
                    }
                }

                UpdateResult.DatasetUpdateFinished -> {
                    // update finished, reset progress state
                    progress = INITIAL_PROGRESS
                }

                UpdateResult.DatasetsUpToDate -> resultDeferred.complete(Result.success())
            }
        }

        if (!resultDeferred.isCompleted) {
            resultDeferred.complete(Result.failure())
        }

        return resultDeferred.await()
    }

    // Create a foreground notification
    private fun createForegroundInfo(datasetType: DatasetType): ForegroundInfo {
        // Notification channel for progress updates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.dataset_update_service),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to cancel the worker
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id) // Pass worker's unique ID

        // Create the notification
        notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(
                when (datasetType) {
                    is DatasetType.RegionPoi -> {
                        applicationContext.getString(R.string.region_dataset_is_being_updated)
                            .format(datasetType.regionName)
                    }

                    DatasetType.Harbours -> {
                        applicationContext.getString(R.string.harbours_dataset_is_being_updated)
                    }
                }
            )
            .setContentText(applicationContext.getString(R.string.waiting_for_server_response))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(
                R.drawable.cancel,
                applicationContext.getString(R.string.cancel),
                cancelIntent
            )
            .setProgress(0, 0, true)
            .setOngoing(true)

        return ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotificationProgress(progress: Int) {
        this.progress = this.progress.takeIf { it != INITIAL_PROGRESS }?.plus(progress) ?: progress

        notificationBuilder
            .setContentText(
                applicationContext.getString(R.string.processed_count).format(this.progress)
            )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateStartedNotificationContentUpdate(datasetType: DatasetType) {
        when (datasetType) {
            is DatasetType.RegionPoi -> {
                notificationBuilder
                    .setContentTitle(
                        applicationContext.getString(R.string.region_dataset_is_being_updated)
                            .format(datasetType.regionName)
                    )
                    .setContentText(
                        applicationContext.getString(R.string.waiting_for_server_response)
                    )
            }

            DatasetType.Harbours -> {
                notificationBuilder
                    .setContentTitle(
                        applicationContext.getString(R.string.harbours_dataset_is_being_updated)
                    )
                    .setContentText(
                        applicationContext.getString(R.string.waiting_for_server_response)
                    )
            }
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}