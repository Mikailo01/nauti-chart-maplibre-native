package com.bytecause.nautichart.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import org.osmdroid.views.MapView

class CustomMapView : MapView {

    private var currentHeading: Float? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        // Initialization logic if needed
    }


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {                // Disallow ScrollView to intercept touch events.
                this.parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP ->                 // Allow ScrollView to intercept touch events.
                this.parent.requestDisallowInterceptTouchEvent(false)
        }

        performClick()
        // Handle MapView's touch events.
        return super.onTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun performLongClick(x: Float, y: Float): Boolean {
        return super.performLongClick(x, y)
    }

    fun setBearing(degrees: Float) {
        this.currentHeading = degrees
        this.invalidate()
    }

    fun getBearing(): Float = this.currentHeading ?: 0f
}