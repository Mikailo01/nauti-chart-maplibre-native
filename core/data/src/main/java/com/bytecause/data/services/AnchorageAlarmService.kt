package com.bytecause.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.bytecause.data.repository.abstractions.AnchorageAlarmRepository
import com.bytecause.domain.model.RunningAnchorageAlarmModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


// TODO("Finish implementation")
@AndroidEntryPoint
class AnchorageAlarmService : LifecycleService(), LocationListener {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var radius: Float = 0f
    private var centerLat: Double = 0.0
    private var centerLng: Double = 0.0
    private lateinit var locationManager: LocationManager

    private var ringtone: Ringtone? = null

    @Inject
    lateinit var anchorageAlarmRepository: AnchorageAlarmRepository

    companion object {
        private const val NOTIFICATION_ID = 4
        private const val CHANNEL_ID = "anchorage_alarm_channel"

        const val EXTRA_RADIUS = "EXTRA_RADIUS"
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
    }

    override fun onLocationChanged(p0: Location) {
        checkIfWithinRadius(p0)
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Get radius and center location from intent extras
        radius = intent?.getFloatExtra(EXTRA_RADIUS, 0f) ?: 0f
        centerLat = intent?.getDoubleExtra(EXTRA_LATITUDE, 0.0) ?: 0.0
        centerLng = intent?.getDoubleExtra(EXTRA_LONGITUDE, 0.0) ?: 0.0

        when (intent?.action) {
            Actions.START.toString() -> {
                startForegroundService()
                startLocationUpdates()
            }

            Actions.STOP.toString() -> {
                stopService()
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.anchorage_alarm),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveRunningAnchorageAlarm() {
        lifecycleScope.launch {
            anchorageAlarmRepository.saveRunningAnchorageAlarm(
                RunningAnchorageAlarmModel(
                    isRunning = true,
                    latitude = centerLat,
                    longitude = centerLng,
                    radius = radius
                )
            )
        }
    }

    private fun removeRunningAnchorageAlarm() {
        lifecycleScope.launch {
            anchorageAlarmRepository.deleteRunningAnchorageAlarm()
        }
    }

    // Start location updates
    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        try {
            // Request location updates from GPS or network provider
            locationManager.requestLocationUpdates(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) LocationManager.FUSED_PROVIDER
                else LocationManager.GPS_PROVIDER,
                5000L, // Minimum time interval between location updates (10 seconds)
                2f,    // Minimum distance between location updates (10 meters)
                this
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun triggerAlarmNotification() {
        notificationBuilder
            .setContentText("You have moved outside the defined radius!")
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        playAlarmSound()
    }

    private fun playAlarmSound() {
        if (ringtone == null) {
            val ringtoneUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // Default alarm sound
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)

            // Optional: Set the volume to maximum (if you want to override)
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )

            ringtone?.play()
        }
    }

    // Check if current location is within the radius of the center
    private fun checkIfWithinRadius(currentLocation: Location) {
        val centerLocation = Location("").apply {
            latitude = centerLat
            longitude = centerLng
        }

        val distance = currentLocation.distanceTo(centerLocation) // Distance in meters

        if (distance > radius) {
            triggerAlarmNotification()
        } else {
            if (ringtone != null) {
                triggerDefaultNotification()
            }
        }
    }

    private fun startForegroundService() {
        // Create an intent that will trigger when the user clicks the "Cancel" action
        val cancelIntent = Intent(this, AnchorageAlarmService::class.java).apply {
            action = Actions.STOP.toString() // action for stopping the service
        }

        // Create a PendingIntent for the cancel action
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.anchorage_alarm_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.cancel, getString(R.string.stop), cancelPendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        saveRunningAnchorageAlarm()
    }

    private fun triggerDefaultNotification() {
        notificationBuilder
            .setContentText(null)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        stopRingtone()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    private fun stopService() {
        ringtone?.stop()
        locationManager.removeUpdates(this)
        removeRunningAnchorageAlarm()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    enum class Actions {
        START,
        STOP
    }
}