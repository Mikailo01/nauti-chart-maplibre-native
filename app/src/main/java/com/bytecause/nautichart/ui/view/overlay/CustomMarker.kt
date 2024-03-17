package com.bytecause.nautichart.ui.view.overlay

import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import com.bytecause.nautichart.interfaces.MapFragmentInterface
import com.bytecause.nautichart.util.MapUtil
import org.osmdroid.util.RectL
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt

class CustomMarker(
    private val mapView: MapView
) : Marker(mapView) {

    private var markerClickListener: MapFragmentInterface? = null

    private var mDisplayed: Boolean = false
    private val mRect = Rect()
    private val mOrientedMarkerRect = Rect()

    private var _type: CustomMarkerType = CustomMarkerType.CustomPoi
    val type get() = _type

    private var _drawableId: Int? = null
    val drawableId get() = _drawableId

    private var _drawableColor: Int? = null
    val drawableColor get() = _drawableColor

    private var _drawableResourceName: String? = null
    val drawableResourceName get() = _drawableResourceName

    private var isClicked = false

    fun setOnMarkerClickListener(listener: MapFragmentInterface) {
        this.markerClickListener = listener
    }

    fun isClicked(boolean: Boolean) {
        this.isClicked = boolean
        mapView.invalidate()
    }

    private fun onMarkerClickDefault(marker: CustomMarker?, mapView: MapView?): Boolean {
        marker ?: return false
        mapView ?: return false

        marker.isClicked = true
        markerClickListener?.openMarkerBottomSheet(marker)
        return true
    }

    override fun showInfoWindow() {
        super.showInfoWindow()
    }

    override fun isDraggable(): Boolean {
        super.mDraggable = false
        return false
    }

    override fun draw(canvas: Canvas?, pj: Projection?) {
        if (mIcon == null || !isEnabled || !MapUtil.isPositionInBoundingBox(
                geoPoint = mPosition,
                boundingBox = mapView.boundingBox
            )
        ) return

        pj?.toPixels(mPosition, mPositionPixels)
        drawAt(canvas, mPositionPixels.x, mPositionPixels.y, rotation)
    }

    override fun hitTest(event: MotionEvent?, mapView: MapView?): Boolean {
        event ?: return false
        return mIcon != null && mDisplayed && mOrientedMarkerRect.contains(
            event.x.toInt(),
            event.y.toInt()
        )
    }

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        val touched = hitTest(event, mapView)
        return if (touched) {
            if (mOnMarkerClickListener == null) {
                onMarkerClickDefault(this, mapView)
            } else {
                mOnMarkerClickListener.onMarkerClick(this, mapView)
            }
        } else return false
    }


    override fun drawAt(pCanvas: Canvas?, pX: Int, pY: Int, pOrientation: Float) {
        pCanvas ?: return

        val currentZoom = mapView.zoomLevelDouble

        var markerWidth =
            if (currentZoom >= 9) (mIcon.intrinsicWidth) * 1.1f else mIcon.intrinsicWidth * 0.8f
        var markerHeight =
            if (currentZoom >= 9) (mIcon.intrinsicHeight) * 1.1f else mIcon.intrinsicHeight * 0.8f

        if (isClicked) {
            markerWidth *= 1.3f
            markerHeight *= 1.3f
        }

        val offsetX = pX - (markerWidth * mAnchorU).roundToInt()
        val offsetY = pY - (markerHeight * mAnchorV).roundToInt()
        mRect[offsetX, offsetY, offsetX + markerWidth.toInt()] = offsetY + markerHeight.toInt()
        RectL.getBounds(mRect, pX, pY, pOrientation.toDouble(), mOrientedMarkerRect)
        mDisplayed = Rect.intersects(mOrientedMarkerRect, pCanvas.clipBounds)
        if (!mDisplayed) { // optimization 1: (much faster, depending on the proportions) don't try to display if the Marker is not visible
            return
        }
        if (mAlpha == 0f) {
            return
        }
        if (pOrientation != 0f) { // optimization 2: don't manipulate the Canvas if not needed (about 25% faster) - step 1/2
            pCanvas.save()
            pCanvas.rotate(pOrientation, pX.toFloat(), pY.toFloat())
        }
        mIcon.alpha = (mAlpha * 255).toInt()
        mIcon.bounds = mRect
        mIcon.draw(pCanvas)

        if (pOrientation != 0f) { // optimization 2: step 2/2
            pCanvas.restore()
        }
    }

    fun setMarkerType(type: CustomMarkerType) {
        this._type = type
    }

    fun setDrawableId(id: Int) {
        this._drawableId = id
    }

    fun setDrawableColor(color: Int) {
        this._drawableColor = color
    }

    fun setDrawableResourceName(name: String?) {
        this._drawableResourceName = name
    }

    sealed class CustomMarkerType {
        data object CustomPoi : CustomMarkerType()
        data object VesselMarker : CustomMarkerType()
        data object HarbourMarker : CustomMarkerType()
    }
}