package com.bytecause.nautichart.ui.view.overlay

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bytecause.nautichart.R
import com.bytecause.nautichart.ui.view.custom.CustomMapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

open class CustomMyLocationNewOverlay(
    iMyLocationProvider: IMyLocationProvider,
    private val mapView: CustomMapView
) : MyLocationNewOverlay(iMyLocationProvider, mapView) {

    private val bottomLayerBitmap =
        ContextCompat.getDrawable(mapView.context, R.drawable.map_location_default_view_angle)
            ?.toBitmap()

    private val mDrawPixel = Point()
    private val mAngleViewMatrix = Matrix()
    private var drawState: Int = 0

    override fun drawMyLocation(canvas: Canvas?, pj: Projection?, lastFix: Location?) {
        if (canvas == null || bottomLayerBitmap == null || lastFix == null || pj == null || !mapView.boundingBox.contains(
                myLocation
            )
        ) return

        pj.toPixels(myLocation, mDrawPixel).apply {
            // Calculate the position to draw the bottom layer bitmap centered at the user's location
            val xBottom = x - bottomLayerBitmap.width / 2
            val yBottom = y - bottomLayerBitmap.height / 2

            // Calculate the position to draw the upper layer bitmap centered at the user's location
            val xUpper = x - mDirectionArrowBitmap.width / 2
            val yUpper = y - mDirectionArrowBitmap.height / 2

            mAngleViewMatrix.setRotate(
                when (drawState) {
                    0 -> mapView.getBearing()
                    1 -> pj.orientation * -1
                    else -> return
                },
                x.toFloat(),
                y.toFloat()
            )

            // Draw the bottom layer bitmap at the calculated position
            canvas.apply {
                // save() and restore() is important if we don't want to apply this matrix to all canvas being drawn.
                save()
                concat(mAngleViewMatrix)
                drawBitmap(bottomLayerBitmap, xBottom.toFloat(), yBottom.toFloat(), null)
                restore()
                // Upper layer.
                drawBitmap(mDirectionArrowBitmap, xUpper.toFloat(), yUpper.toFloat(), null)
            }
        }
    }

    fun setDrawState(state: Int) {
        this.drawState = state
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        super.onLocationChanged(location, source)
    }
}