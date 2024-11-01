package com.bytecause.map.sensors

import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

interface BearingSensorListener {
    fun onBearingUpdated(bearing: Int)
}

class BearingSensor {
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val magnetometerReading = FloatArray(3)
    private val accelerometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var sensorManager: SensorManager? = null
        private set

    private var listener: BearingSensorListener? = null

    fun register(
        activity: Activity,
        listener: BearingSensorListener
    ) {
        this.listener = listener

        sensorManager =
            activity.getSystemService(
                SENSOR_SERVICE
            ) as SensorManager

        sensorManager?.run {
            accelerometer =
                getDefaultSensor(
                    TYPE_ACCELEROMETER
                )
            magnetometer =
                getDefaultSensor(
                    TYPE_MAGNETIC_FIELD
                )

            registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun onResume() {
        sensorManager?.run {
            registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun onPause() {
        sensorManager?.unregisterListener(sensorListener)
    }

    fun unregister() {
        sensorManager?.unregisterListener(sensorListener)
        sensorManager = null
    }

    private val sensorListener =
        object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                p0 ?: return

                if (p0.sensor.type == TYPE_ACCELEROMETER) {
                    System.arraycopy(
                        p0.values,
                        0,
                        accelerometerReading,
                        0,
                        accelerometerReading.size
                    )
                } else if (p0.sensor.type == TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(p0.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }

                // Check if we can calculate the rotation matrix
                if (SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerReading,
                        magnetometerReading
                    )
                ) {
                    // Get orientation from the rotation matrix
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    // orientationAngles[0] is the azimuth (bearing) in radians, so convert to degrees
                    val azimuthInRadians = orientationAngles[0]
                    val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                    // Normalize azimuth to [0, 360] range
                    val bearing = Math.round(
                        if (azimuthInDegrees < 0) azimuthInDegrees + 360 else azimuthInDegrees
                    )

                    listener?.onBearingUpdated(bearing)
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
        }
}
