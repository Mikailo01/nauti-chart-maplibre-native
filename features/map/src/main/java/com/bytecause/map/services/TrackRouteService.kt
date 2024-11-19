package com.bytecause.map.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bytecause.core.resources.R
import com.bytecause.data.services.Actions
import com.bytecause.domain.abstractions.TrackRouteRepository
import com.bytecause.domain.model.RouteRecordModel
import com.bytecause.map.ui.model.TrackRouteServiceState
import com.bytecause.map.util.MapUtil
import com.bytecause.util.map.MapUtil.calculateAndSumDistance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

@AndroidEntryPoint
class TrackRouteService : LifecycleService() {

    companion object {
        private const val NOTIFICATION_ID = 5
        private const val CHANNEL_ID = "track_route_channel"
        private const val LOCATION_UPDATE_INTERVAL = 10_000L
        private const val DISTANCE_DELTA = 0.0

        const val NAME = "name_string_extra"
        const val DESCRIPTION = "description_string_extra"
        const val DISCARD = "discard_boolean_extra"

        private val _trackServiceState = MutableStateFlow(TrackRouteServiceState())
        val trackServiceState: StateFlow<TrackRouteServiceState> = _trackServiceState
    }

    @Inject
    lateinit var trackRouteRepository: dagger.Lazy<TrackRouteRepository>

    private var startTime: Long = 0L

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.lastLocation?.let { lastLocation ->
                trackServiceState.value.capturedPoints.lastOrNull()?.let { previousLocation ->
                    if (!MapUtil.arePointsWithinDelta(
                            point1 = LatLng(
                                latitude = previousLocation.first,
                                longitude = previousLocation.second
                            ),
                            point2 = LatLng(
                                latitude = lastLocation.latitude,
                                longitude = lastLocation.longitude
                            ),
                            deltaInMeters = DISTANCE_DELTA
                        )
                    ) {
                        _trackServiceState.update {
                            it.copy(
                                capturedPoints = it.capturedPoints + listOf(
                                    lastLocation.latitude to lastLocation.longitude
                                ),
                                speed = it.speed + mapOf(
                                    (lastLocation.latitude to lastLocation.longitude) to lastLocation.speed
                                )
                            )
                        }
                    }
                } ?: run {
                    _trackServiceState.update {
                        it.copy(
                            capturedPoints = it.capturedPoints + listOf(
                                lastLocation.latitude to lastLocation.longitude
                            ),
                            speed = it.speed + mapOf(
                                (lastLocation.latitude to lastLocation.longitude) to lastLocation.speed
                            )
                        )
                    }
                }
            }
        }
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.route_tracker),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        // Create an intent that will trigger when the user clicks the "Cancel" action
        val cancelIntent = Intent(this, TrackRouteService::class.java).apply {
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
            .setContentTitle(getString(R.string.route_tracker_running))
            .setSmallIcon(R.drawable.baseline_timeline_24)
            .addAction(R.drawable.cancel, getString(R.string.stop), cancelPendingIntent)
            .setOngoing(true)

        // Save current time to able to infer running duration later
        startTime = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        _trackServiceState.update { it.copy(isRunning = true) }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL
        )
            .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL)
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

    private fun stopService() {
        _trackServiceState.value = TrackRouteServiceState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun saveRecord(name: String, description: String) {
        trackRouteRepository.get().saveRecord(
            RouteRecordModel(
                name = name,
                description = description,
                distance = calculateAndSumDistance(trackServiceState.value.capturedPoints),
                startTime = startTime,
                dateCreated = System.currentTimeMillis(),
                points = trackServiceState.value.capturedPoints,
                speed = trackServiceState.value.speed
            )
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.START.toString() -> {
                startForegroundService()
                startLocationUpdates()
            }

            Actions.STOP.toString() -> {
                lifecycleScope.launch {
                    val (name, description) = intent.getStringExtra(NAME) to intent.getStringExtra(
                        DESCRIPTION
                    )
                    val shouldDiscard = intent.getBooleanExtra(DISCARD, false)

                    if (!shouldDiscard && name != null) {
                        saveRecord(
                            name = name,
                            description = description ?: ""
                        )
                    }
                }.invokeOnCompletion {
                    stopService()
                }
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
}
