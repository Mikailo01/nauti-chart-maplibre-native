package com.bytecause.nautichart

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bytecause.nautichart.di.factory.CustomWorkerFactory
import com.bytecause.nautichart.worker.DeletePoiSearchRadiusCacheWorker
import com.bytecause.nautichart.worker.initializer.UpdateExpiredDatasetsWorkerInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POI_RADIUS_SEARCH_CACHE_CLEARANCE_INTERVAL = 7L
private const val POI_RADIUS_SEARCH_CACHE_CLEARANCE_WORK_NAME = "DeletePoiSearchRadiusCache"

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var updateExpiredDatasetsWorkerInitializer: UpdateExpiredDatasetsWorkerInitializer

    @Inject
    lateinit var customWorkerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(customWorkerFactory).build()

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        coroutineScope.launch {
            try {
                updateExpiredDatasetsWorkerInitializer.initialize(this@App)
            } finally {
                coroutineScope.cancel()
            }
        }

        val workRequest = PeriodicWorkRequestBuilder<DeletePoiSearchRadiusCacheWorker>(
            POI_RADIUS_SEARCH_CACHE_CLEARANCE_INTERVAL, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                POI_RADIUS_SEARCH_CACHE_CLEARANCE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // Ensures only one instance of the work is enqueued
                workRequest
            )
    }
}