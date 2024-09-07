package com.bytecause.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.util.poi.PoiUtil.excludeAmenityObjectsFilterList
import com.bytecause.util.poi.PoiUtil.searchTypesStringList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegionPoiUpdateService : LifecycleService() {

    private val channelId = "region_poi_update_channel"
    private var progress: Int = -1

    private var regionId: Int = -1

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var getPoiResultByRegionUseCase: GetPoiResultByRegionUseCase

    companion object {
        const val NOTIFICATION_ID = 1
        const val REGION_ID_PARAM = "regionId"
        const val REGION_NAME_PARAM = "regionName"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.START.toString() -> {

                regionId = intent.getIntExtra(REGION_ID_PARAM, -1)
                val regionName = intent.getStringExtra(REGION_NAME_PARAM) ?: ""

                startForegroundServiceWithProgress()
                downloadRegionPoisData(regionId = regionId, regionName = regionName)
            }

            Actions.STOP.toString() -> {
                stopService()
            }
        }
        return START_STICKY // Ensures that the service continues running until explicitly stopped
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Region POI Update Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for updating region poi data"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithProgress() {
        // Create an intent that will trigger when the user clicks the "Cancel" action
        val cancelIntent = Intent(this, RegionPoiUpdateService::class.java).apply {
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
            .setContentTitle(getString(R.string.updating_region))
            .setContentText(getString(R.string.waiting_for_server_response))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(0, 0, true)
            .addAction(R.drawable.cancel, getString(R.string.cancel), cancelPendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun downloadRegionPoisData(regionId: Int, regionName: String) {
        lifecycleScope.launch {
            val query = OverpassQueryBuilder
                .format(OverpassQueryBuilder.FormatTypes.JSON)
                .timeout(240)
                .region(regionName)
                .type(OverpassQueryBuilder.Type.Node)
                .search(
                    com.bytecause.domain.util.SearchTypes.UnionSet(searchTypesStringList)
                        .filterNot(
                            emptyList(),
                            excludeAmenityObjectsFilterList,
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList()
                        )
                )
                .build()

            ServiceApiResultListener.postEvent(ServiceEvent.RegionPoiUpdateStarted(regionId))

            getPoiResultByRegionUseCase(query = query, regionId = regionId).collect { result ->
                when (result) {
                    is ApiResult.Progress -> {
                        result.progress?.let { progress ->
                            ServiceApiResultListener.postEvent(
                                ServiceEvent.RegionPoiUpdate(
                                    regionId,
                                    result
                                )
                            )
                            updateNotificationProgress(progress)
                        }
                    }

                    is ApiResult.Success -> {
                        ServiceApiResultListener.postEvent(
                            ServiceEvent.RegionPoiUpdate(
                                regionId,
                                result
                            )
                        )
                        stopService()
                    }

                    is ApiResult.Failure -> {
                        ServiceApiResultListener.postEvent(
                            ServiceEvent.RegionPoiUpdate(
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

    private fun updateNotificationProgress(progress: Int) {
        this.progress = this.progress.takeIf { it != -1 }?.plus(progress) ?: progress

        notificationBuilder
            .setContentText(
                getString(R.string.processed_count).format(this.progress)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopService() {
        ServiceApiResultListener.postEvent(ServiceEvent.RegionPoiUpdateCancelled(regionId))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    enum class Actions {
        START,
        STOP
    }
}