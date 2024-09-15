package com.bytecause.nautichart

import android.app.Application
import androidx.work.Configuration
import com.bytecause.nautichart.di.factory.CustomUpdateExpiredDatasetsWorkerFactory
import com.bytecause.nautichart.worker.UpdateExpiredDatasetsWorkerInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var updateExpiredDatasetsWorkerInitializer: UpdateExpiredDatasetsWorkerInitializer

    @Inject
    lateinit var updateExpiredDatasetsWorkerFactory: CustomUpdateExpiredDatasetsWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(updateExpiredDatasetsWorkerFactory).build()

    private val workerScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        workerScope.launch {
            try {
                updateExpiredDatasetsWorkerInitializer.initialize(this@App)
            } finally {
                workerScope.cancel()
            }
        }
    }
}