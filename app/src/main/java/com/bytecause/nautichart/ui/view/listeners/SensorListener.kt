package com.bytecause.nautichart.ui.view.listeners

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import com.bytecause.nautichart.ui.view.custom.CustomMapView
import kotlin.math.abs

private const val THRESHOLD = 5.0

class SensorListener(private val customMapView: CustomMapView) : SensorEventListener {

    private var sensorRegistered: Boolean = false
    private var isUpdating: Boolean = false

    private val sensorManager =
        customMapView.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var alpha = 0.2f // Smoothing factor (0 < alpha < 1)

    private var smoothedHeading: Float = 0f

    private var lastHeading: Float = 0f

    private var buttonState = 0

    init {
        val orientationEventListener = object : OrientationEventListener(customMapView.context) {
            override fun onOrientationChanged(orientation: Int) {

                updateSmoothedHeading()
                setDegreesValue()
            }
        }
        orientationEventListener.enable()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            if (isUpdating || !sensorRegistered) return
            isUpdating = true

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            updateSmoothedHeading()

            val headingDifference = abs(smoothedHeading - lastHeading)
            if (headingDifference > THRESHOLD) {
                lastHeading = customMapView.getBearing()
                setDegreesValue()
            }
            isUpdating = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateSmoothedHeading() {
        val newHeading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

        if (abs(newHeading - lastHeading) >= 10) {
            if (alpha < 0.07) alpha += 0.01f
        } else alpha = 0.02f

        smoothedHeading = if (abs(newHeading - smoothedHeading) < 180) {
            alpha * newHeading + (1 - alpha) * smoothedHeading
        } else {
            if (newHeading > smoothedHeading) {
                alpha * (newHeading - 360) + (1 - alpha) * smoothedHeading
            } else {
                alpha * (newHeading + 360) + (1 - alpha) * smoothedHeading
            }
        }
    }

    fun registerSensorListener() {
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(
            this,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorRegistered = true
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
        sensorRegistered = false
    }

    private fun setDegreesValue() {
        if (buttonState == 1) {
            customMapView.mapOrientation = -smoothedHeading
        } else {
            customMapView.apply {
                setBearing(smoothedHeading)
            }
        }
    }

    fun buttonState(state: Int) {
        this.buttonState = state
    }
}