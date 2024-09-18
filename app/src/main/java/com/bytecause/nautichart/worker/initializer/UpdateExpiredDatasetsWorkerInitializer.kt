package com.bytecause.nautichart.worker.initializer

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bytecause.nautichart.worker.UpdateExpiredDatasetsWorker
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.NetworkType
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateExpiredDatasetsWorkerInitializer @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun initialize(context: Context) {
        val preferredNetworkType =
            when (userPreferencesRepository.getAutoUpdatesNetworkPreference().first()) {
                NetworkType.WIFI_ONLY -> androidx.work.NetworkType.UNMETERED
                NetworkType.WIFI_AND_MOBILE_DATA -> androidx.work.NetworkType.NOT_ROAMING
                NetworkType.DISABLED -> return // auto updates disabled, don't start worker
            }

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(preferredNetworkType)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UpdateExpiredDatasetsWorker>()
            .setConstraints(networkConstraints)
            // if IOException is thrown worker will start dataset update again, after some period
            // which is being extended linearly on each IOException failure.
            // The period starts on 10s, so period will be 10s + 10s, 20s + 10s, 30s + 10s and so on,
            // until update is finished successfully or maximum time is exceeded (5 hours).
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
