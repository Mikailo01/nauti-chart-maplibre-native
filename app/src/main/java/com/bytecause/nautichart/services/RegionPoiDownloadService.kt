package com.bytecause.nautichart.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.bytecause.data.services.Actions
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.domain.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOTIFICATION_ID = 1

@AndroidEntryPoint
class RegionPoiDownloadService : LifecycleService() {

    private val channelId = "region_poi_update_channel"
    private var progress: Int = -1

    private var regionId: Int = -1
    private var regionName: String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var getPoiResultByRegionUseCase: GetPoiResultByRegionUseCase

    companion object {
        const val REGION_ID_PARAM = "regionId"
        const val REGION_NAME_PARAM = "regionName"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.START.toString() -> {

                regionId = intent.getIntExtra(REGION_ID_PARAM, -1)
                regionName = intent.getStringExtra(REGION_NAME_PARAM) ?: ""

                startForegroundServiceWithProgress()
                downloadRegionPoisData(regionId = regionId, regionName = regionName)
            }

            Actions.STOP.toString() -> {
                ServiceApiResultListener.postEvent(ServiceEvent.RegionPoiDownloadCancelled(regionId = regionId))
                stopService()
            }
        }
        return START_STICKY // Ensures that the service continues running until explicitly stopped
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.region_update),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithProgress() {
        // Create an intent that will trigger when the user clicks the "Cancel" action
        val cancelIntent = Intent(this, RegionPoiDownloadService::class.java).apply {
            action = Actions.STOP.toString() // Define the action for stopping the service
        }

        // Create a PendingIntent for the cancel action
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.downloading_region).format(regionName))
            .setContentText(getString(R.string.waiting_for_server_response))
            .setSmallIcon(R.drawable.region)
            .setProgress(0, 0, true)
            .addAction(R.drawable.cancel, getString(R.string.cancel), cancelPendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun downloadRegionPoisData(regionId: Int, regionName: String) {
        lifecycleScope.launch {
            val query = OverpassQueryBuilder.format(OverpassQueryBuilder.FormatTypes.JSON)
                .timeout(240)
                .region(regionName)
                .type(OverpassQueryBuilder.Type.Node)
                .search(
                    com.bytecause.domain.util.SearchTypes.UnionSet(Util.searchTypesStringList)
                        .filterNot(
                            emptyList(),
                            Util.excludeAmenityObjectsFilterList,
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList()
                        )
                )
                .build()

            ServiceApiResultListener.postEvent(ServiceEvent.RegionPoiDownloadStarted(regionId))

            getPoiResultByRegionUseCase(query = query, regionId = regionId).collect { result ->
                when (result) {
                    is ApiResult.Progress -> {
                        result.progress?.let { progress ->

                            this@RegionPoiDownloadService.progress =
                                this@RegionPoiDownloadService.progress.takeIf { it != -1 }
                                    ?.plus(progress) ?: progress

                            ServiceApiResultListener.postEvent(
                                ServiceEvent.RegionPoiDownload(
                                    regionId,
                                    ApiResult.Progress<Nothing>(progress = this@RegionPoiDownloadService.progress)
                                )
                            )
                            updateNotificationProgress()
                        }
                    }

                    is ApiResult.Success -> {
                        ServiceApiResultListener.postEvent(
                            ServiceEvent.RegionPoiDownload(
                                regionId,
                                result
                            )
                        )
                        stopService()
                    }

                    is ApiResult.Failure -> {
                        ServiceApiResultListener.postEvent(
                            ServiceEvent.RegionPoiDownload(
                                regionId,
                                result
                            )
                        )
                        stopService()
                    }
                }
            }
        }
    }

    private fun updateNotificationProgress() {
        notificationBuilder
            .setContentText(
                getString(R.string.processed_count).format(this.progress)
            )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopService() {
        ServiceApiResultListener.postEvent(ServiceEvent.RegionPoiDownloadCancelled(regionId))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}