package com.bytecause.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

data class RunningAnchorageAlarm(
    val isRunning: Boolean = false,
    val radius: Float = 5f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@AndroidEntryPoint
class AnchorageAlarmService : LifecycleService(), LocationListener {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 10000L // Update interval 10 seconds
    )
        .setMinUpdateIntervalMillis(2000L)
        .setWaitForAccurateLocation(true)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.lastLocation?.let {
                checkIfWithinRadius(it)
            }
        }
    }

    private var ringtone: Ringtone? = null

    companion object {
        private val _runningAnchorageAlarm: MutableStateFlow<RunningAnchorageAlarm> =
            MutableStateFlow(
                RunningAnchorageAlarm()
            )
        val runningAnchorageAlarm: StateFlow<RunningAnchorageAlarm> =
            _runningAnchorageAlarm.asStateFlow()

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        resetState()
    }

    private fun loadState(intent: Intent) {
        lifecycleScope.launch {
            _runningAnchorageAlarm.emit(
                RunningAnchorageAlarm(
                    isRunning = true,
                    radius = intent.getFloatExtra(EXTRA_RADIUS, 5f),
                    latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
                    longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                )
            )
        }
    }

    private fun resetState() {
        lifecycleScope.launch {
            _runningAnchorageAlarm.emit(RunningAnchorageAlarm())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.START.toString() -> {
                // load state only if service is starting
                loadState(intent)

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
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Start location updates
    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun triggerAlarmNotification() {
        notificationBuilder
            .setContentText(getString(R.string.you_have_moved_outside_the_defined_radius))
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
            latitude = runningAnchorageAlarm.value.latitude
            longitude = runningAnchorageAlarm.value.longitude
        }

        val distance = currentLocation.distanceTo(centerLocation) // Distance in meters

        if (distance > runningAnchorageAlarm.value.radius) {
            triggerAlarmNotification()
        } else {
            if (ringtone != null) {
                stopRingtone()
            }
            updateNotificationTextContent(
                location = currentLocation,
                distanceFromCenterPoint = round(distance * 10) / 10 // rounds on first decimal place
            )
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
    }


    private fun updateNotificationTextContent(location: Location, distanceFromCenterPoint: Float) {
        notificationBuilder
            .setContentText(
                getString(R.string.radius) + " ${runningAnchorageAlarm.value.radius.toInt()} M"
                        + " | "
                        + getString(R.string.accuracy) + " ${location.accuracy.toInt()}" + " M |"
                        + "\n" + getString(R.string.distance_from_anchor) + " $distanceFromCenterPoint M"
            )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    private fun stopService() {
        ringtone?.stop()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    enum class Actions {
        START,
        STOP
    }
}