package com.bytecause.nautichart.ui.view.overlay

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import org.osmdroid.api.IGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class TargetOverlay(private val bitmap: BitmapDrawable): Overlay() {

    private val center = Point()
    private var centerGeoPoint: IGeoPoint? = null

    override fun draw(pCanvas: Canvas?, pMapView: MapView?, pShadow: Boolean) {
        super.draw(pCanvas, pMapView, pShadow)

        if (pCanvas == null) return

        if (!pShadow) {

            val mapViewWidth = pMapView?.width ?: 0
            val mapViewHeight = pMapView?.height ?: 0

            // Calculate the screen position of the center
            center.set(mapViewWidth / 2, mapViewHeight / 2 + 28)

            val proj = pMapView?.projection

            // Calculate the geographical position of the center
            centerGeoPoint = pMapView?.projection?.fromPixels(center.x, center.y)

            proj?.save(pCanvas, false, true)
            // Draw the custom drawable at the center of the map view
            centerGeoPoint?.let {
                val x = center.x - bitmap.intrinsicWidth / 2
                val y = center.y - bitmap.intrinsicHeight
                bitmap.setBounds(x, y, x + bitmap.intrinsicWidth, y + bitmap.intrinsicHeight)
                bitmap.draw(pCanvas)
            }
            proj?.restore(pCanvas, true)
        }
    }
}