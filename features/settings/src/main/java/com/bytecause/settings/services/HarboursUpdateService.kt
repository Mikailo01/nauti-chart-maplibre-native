package com.bytecause.settings.services

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
import com.bytecause.data.services.Actions
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.usecase.UpdateHarboursUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOTIFICATION_ID = 2

@AndroidEntryPoint
class HarboursUpdateService : LifecycleService() {

    private val channelId = "harbours_update_channel"
    private var progress: Int = -1

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var updateHarboursUseCase: UpdateHarboursUseCase

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.START.toString() -> {
                startForegroundServiceWithProgress()
                updateHarboursData()
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
                getString(R.string.harbours_update),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithProgress() {
        // Create an intent that will trigger when the user clicks the "Cancel" action
        val cancelIntent = Intent(this, HarboursUpdateService::class.java).apply {
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
            .setContentTitle(getString(R.string.updating_harbours))
            .setContentText(getString(R.string.waiting_for_server_response))
            .setSmallIcon(R.drawable.harbour_marker_icon)
            .setProgress(0, 0, true)
            .addAction(R.drawable.cancel, getString(R.string.cancel), cancelPendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateHarboursData() {
        lifecycleScope.launch {

            ServiceApiResultListener.postEvent(ServiceEvent.HarboursUpdateStarted)

            updateHarboursUseCase(forceUpdate = true).collect { result ->
                when (result) {
                    is ApiResult.Progress -> {
                        result.progress?.let { progress ->

                            this@HarboursUpdateService.progress =
                                this@HarboursUpdateService.progress.takeIf { it != -1 }
                                    ?.plus(progress) ?: progress

                            ServiceApiResultListener.postEvent(
                                ServiceEvent.HarboursUpdate(
                                    ApiResult.Progress<Nothing>(
                                        progress = this@HarboursUpdateService.progress
                                    )
                                )
                            )
                            updateNotificationProgress()
                        }
                    }

                    is ApiResult.Success -> {
                        ServiceApiResultListener.postEvent(ServiceEvent.HarboursUpdate(result))
                        stopService()
                    }

                    is ApiResult.Failure -> {
                        ServiceApiResultListener.postEvent(ServiceEvent.HarboursUpdate(result))
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
        ServiceApiResultListener.postEvent(ServiceEvent.HarboursUpdateCancelled)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}