package com.bytecause.map.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.bytecause.data.model.AnchorageMovementTrackModel
import com.bytecause.data.model.RunningAnchorageAlarm
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.data.repository.abstractions.AnchorageMovementTrackRepository
import com.bytecause.data.services.Actions
import com.bytecause.map.util.MapUtil
import com.bytecause.util.extensions.toFirstDecimal
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject


@AndroidEntryPoint
class AnchorageAlarmService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var anchorageAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository

    @Inject
    lateinit var anchorageMovementTrackRepository: AnchorageMovementTrackRepository

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest

    private var alarmDelay: Long = 0L
    private var alarmJob: Job? = null

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private val vibratePattern = longArrayOf(0L, 1000L, 1000L)

    private var shouldTrackMovement: Boolean = false

    private lateinit var batteryReceiver: BroadcastReceiver

    private val handler = Handler(Looper.getMainLooper())

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.lastLocation?.let { lastLocation ->
                checkIfWithinRadius(lastLocation)

                if (shouldTrackMovement) {
                    // check if list contains at least 2 elements to be able to compare them
                    p0.locations.takeIf { locationList -> locationList.size >= 2 }
                        ?.let { locations ->
                            val previousLocation = locations[locations.size - 2]

                            if (!MapUtil.arePointsWithinDelta(
                                    point1 = LatLng(
                                        latitude = previousLocation.latitude,
                                        longitude = previousLocation.longitude
                                    ),
                                    point2 = LatLng(
                                        latitude = lastLocation.latitude,
                                        longitude = lastLocation.longitude
                                    )
                                )
                            ) {
                                saveCurrentPositionForTracking(lastLocation)
                            }
                        } ?: run {
                        saveCurrentPositionForTracking(lastLocation)
                    }
                }
            }
        }
    }

    private val checkRingtoneRunnable = object : Runnable {
        override fun run() {
            if (ringtone?.isPlaying != true) {
                ringtone?.play()
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun saveCurrentPositionForTracking(position: Location) {
        lifecycleScope.launch {
            anchorageMovementTrackRepository.insertPosition(
                AnchorageMovementTrackModel(
                    latitude = position.latitude,
                    longitude = position.longitude
                )
            )
        }
    }

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

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        lifecycleScope.launch {
            launch {
                anchorageAlarmPreferencesRepository.getAlarmDelay().collect { delay ->
                    alarmDelay = delay
                }
            }

            launch {
                anchorageAlarmPreferencesRepository.getTrackBatteryState()
                    .collect { trackBatteryState ->
                        if (trackBatteryState) {
                            if (::batteryReceiver.isInitialized) return@collect

                            batteryReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    val batteryLevel =
                                        intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                                    val batteryScale =
                                        intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                                    val batteryPct = batteryLevel / batteryScale.toFloat() * 100

                                    // if battery's level is under 20 % start alarm
                                    if (batteryPct < 20) {
                                        startAlarmSoundAndVibration()
                                    }
                                }
                            }

                            val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                            registerReceiver(batteryReceiver, batteryIntentFilter)
                        } else {
                            if (!::batteryReceiver.isInitialized) return@collect

                            unregisterReceiver(batteryReceiver)
                        }
                    }
            }

            launch {
                anchorageAlarmPreferencesRepository.getTrackMovementState().collect { shouldTrack ->
                    shouldTrackMovement = shouldTrack
                }
            }

            launch {
                // clear previously cached vessel's movement points
                anchorageMovementTrackRepository.clear()
            }
        }
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

            launch {
                anchorageMovementTrackRepository.clear()
            }
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

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        // if update interval is changed, then this observer will create new location request with
        // updated intervals
        combine(
            anchorageAlarmPreferencesRepository.getMaxUpdateInterval(),
            anchorageAlarmPreferencesRepository.getMinUpdateInterval()
        ) { maxInterval, minInterval ->

            locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, maxInterval
            )
                .setMinUpdateIntervalMillis(minInterval)
                .setWaitForAccurateLocation(true)
                .build()

            fusedLocationClient?.let { locationClient ->
                locationClient.removeLocationUpdates(locationCallback)
                locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } ?: run {
                // fusedLocationClient is null, make initialization
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    .apply {
                        requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                    }
            }
        }
            .launchIn(lifecycleScope)
    }

    private fun triggerAlarmNotification() {
        alarmJob = lifecycleScope.launch {
            delay(alarmDelay)

            notificationBuilder
                .setContentText(getString(R.string.you_have_moved_outside_the_defined_radius))
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

            startAlarmSoundAndVibration()
        }
    }

    @Suppress("MissingPermission")
    private fun startAlarmSoundAndVibration() {
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

            // setup vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                // backward compatibility for Android API < 31,
                // VibratorManager was only added on API level 31 release.
                @Suppress("Deprecation")
                vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            // start vibration
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            vibratePattern,
                            0
                        )
                    )
                } else {
                    @Suppress("Deprecation")
                    vibrator?.vibrate(vibratePattern, 0)
                }
            }

            // set looping
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            } else {
                // isLooping is available from API 28 and above, so on lower APIs we need to use looper,
                // which will check every second if ringtone is playing or not
                handler.post(checkRingtoneRunnable)
            }

            ringtone?.play()
        }
    }

    @Suppress("MissingPermission")
    private fun stopAlarmSoundAndVibration() {
        handler.removeCallbacks(checkRingtoneRunnable)

        vibrator?.cancel()
        vibrator = null

        ringtone?.stop()
        ringtone = null
    }

    // Check if current location is within the radius of the center
    private fun checkIfWithinRadius(currentLocation: Location) {
        val centerLocation = Location("").apply {
            latitude = runningAnchorageAlarm.value.latitude
            longitude = runningAnchorageAlarm.value.longitude
        }

        val distance = currentLocation.distanceTo(centerLocation) // Distance in meters

        if (distance > runningAnchorageAlarm.value.radius) {
            // don't trigger alarm notification if alarm alert is scheduled, because it would be
            // rescheduled again and alarm alert would never start
            if (alarmJob?.isActive == true) return
            triggerAlarmNotification()
        } else {
            // if there is scheduled alarm alert and location has been correctly aligned back inside
            // the radius, cancel it
            if (alarmJob != null) {
                alarmJob?.cancel()
                alarmJob = null
                stopAlarmSoundAndVibration()
            }

            updateNotificationTextContent(
                location = currentLocation,
                distanceFromCenterPoint = distance.toFirstDecimal()
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
            .setSmallIcon(R.drawable.anchorage_alarm)
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

    private fun stopService() {
        stopAlarmSoundAndVibration()
        fusedLocationClient?.removeLocationUpdates(locationCallback)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}