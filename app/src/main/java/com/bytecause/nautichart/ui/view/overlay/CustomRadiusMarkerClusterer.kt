package com.bytecause.nautichart.ui.view.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bytecause.nautichart.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.sqrt

open class CustomRadiusMarkerClusterer(context: Context) : CustomMarkerClusterer() {

    private var mMaxClusteringZoomLevel = 17.0
    private var mRadiusInPixels = 100
    private var mRadiusInMeters = 0.0
    protected var mAnchorU = Marker.ANCHOR_CENTER
    protected var mAnchorV: Float = Marker.ANCHOR_CENTER
    protected var mDensityDpi = 0
    protected var mTextPaint: Paint? = null
    private val mClonedMarkers: MutableList<CustomMarker> = mutableListOf()
    private var mAnimated = false

    /** anchor point to draw the number of markers inside the cluster icon  */
    var mTextAnchorU = Marker.ANCHOR_CENTER

    /** anchor point to draw the number of markers inside the cluster icon  */
    var mTextAnchorV: Float = Marker.ANCHOR_CENTER

    fun setTextColor(color: Int) {
        mTextPaint?.color = color
    }

    init {
        mTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12 * context.resources.displayMetrics.density
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        ContextCompat.getDrawable(context, R.drawable.harbours_clusterer_icon)?.apply {
            setIcon(this.toBitmap())
        }
        mDensityDpi = context.resources.displayMetrics.densityDpi
        mAnimated = true
    }

    fun <T> addAll(markerList: List<T>?) {
        if (markerList.isNullOrEmpty() && markerList !is Marker) return
        for (marker in markerList) {
            super.add(marker as CustomMarker) // Add each marker to the parent class's marker list
        }
    }

    override fun getItems(): List<CustomMarker> = mItems

    fun clearItems() {
        mItems.clear()
    }

    fun getTextPaint(): Paint? {
        return mTextPaint
    }

    fun setRadius(radius: Int) {
        mRadiusInPixels = radius
    }

    fun setMaxClusteringZoomLevel(zoom: Double) {
        mMaxClusteringZoomLevel = zoom
    }

    override fun buildClusterMarker(cluster: CustomStaticCluster?, mapView: MapView?): CustomMarker? {
        cluster ?: return null
        mapView ?: return null

        val m = CustomMarker(mapView)
        m.position = cluster.getPosition()
        m.setInfoWindow(null)
        m.setAnchor(mAnchorU, mAnchorV)

        mClusterIcon?.let { clusterIcon ->
            val finalIcon = Bitmap.createBitmap(
                clusterIcon.getScaledWidth(mDensityDpi),
                clusterIcon.getScaledHeight(mDensityDpi), clusterIcon.config
            )
            val iconCanvas = Canvas(finalIcon)
            iconCanvas.drawBitmap(clusterIcon, 0f, 0f, null)
            val text = "" + cluster.getSize()
            mTextPaint?.let { textPaint ->
                val textHeight: Int = (textPaint.descent() + textPaint.ascent()).toInt()
                iconCanvas.drawText(
                    text,
                    mTextAnchorU * finalIcon.width,
                    mTextAnchorV * finalIcon.height - textHeight / 2,
                    textPaint
                )
            }
            m.icon = BitmapDrawable(mapView.context.resources, finalIcon)
        }

        return m
    }

    override fun renderer(
        clusters: MutableList<CustomStaticCluster>?,
        canvas: Canvas?,
        mapView: MapView?
    ) {
        clusters ?: return

        for (cluster in clusters) {
            if (cluster.getSize() == 1) {
                //cluster has only 1 marker => use it as it is:
                cluster.setMarker(cluster.getItem(0))
            } else {
                //only draw 1 Marker at Cluster center, displaying number of Markers contained
                val m = buildClusterMarker(cluster, mapView)
                cluster.setMarker(m)
            }
        }
    }

    // Return list with all clusterers.
    override fun clusterer(mapView: MapView?): MutableList<CustomStaticCluster> {
        val clusters = mutableListOf<CustomStaticCluster>()
        mapView?.let {
            convertRadiusToMeters(mapView)

            mClonedMarkers.apply {
                // Shallow copy.
                addAll(mItems)
                while (this.isNotEmpty()) {
                    val m: CustomMarker = this[0]
                    val cluster: CustomStaticCluster = createCluster(m, mapView)
                    clusters.add(cluster)
                }
            }
            return clusters
        } ?: return mutableListOf()
    }

    // Map each marker to the corresponding clusterer based on radius.
    private fun createCluster(m: CustomMarker, mapView: MapView): CustomStaticCluster {
        val clusterPosition = m.position
        val cluster = CustomStaticCluster(clusterPosition)
        cluster.add(m)
        mClonedMarkers.remove(m)
        if (mapView.zoomLevelDouble > mMaxClusteringZoomLevel) {
            //above max level => block clustering:
            return cluster
        }
        val markerIterator = mClonedMarkers.iterator()
        while (markerIterator.hasNext()) {
            val neighbour = markerIterator.next()
            val distance = clusterPosition.distanceToAsDouble(neighbour.position)
            if (distance <= mRadiusInMeters) {
                cluster.add(neighbour)
                markerIterator.remove()
            }
        }

        return cluster
    }

    private fun zoomOnCluster(mapView: MapView, cluster: CustomStaticCluster) {
        var bb = cluster.getBoundingBox()
        bb?.let {
            if (it.latNorth != it.latSouth || it.lonEast != it.lonWest) {
                bb = it.increaseByScale(1.15f)
                mapView.zoomToBoundingBox(bb, true)
            } else  // all points exactly at the same place:
                mapView.setExpectedCenter(it.centerWithDateLine)
        }
    }

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        mapView ?: return false
        for (cluster in reversedClusters()) {
            cluster.getMarker()?.let {
                if (it.onSingleTapConfirmed(event, mapView)) {
                    if (mAnimated && cluster.getSize() > 1) zoomOnCluster(mapView, cluster)
                    return true
                }
            }
        }
        return false
    }

    private fun convertRadiusToMeters(mapView: MapView) {
        val mScreenRect = mapView.getIntrinsicScreenRect(null)
        val screenWidth = mScreenRect.right - mScreenRect.left
        val screenHeight = mScreenRect.bottom - mScreenRect.top
        val bb = mapView.boundingBox
        val diagonalInMeters = bb.diagonalLengthInMeters
        val diagonalInPixels =
            sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble())
        val metersInPixel = diagonalInMeters / diagonalInPixels
        mRadiusInMeters = mRadiusInPixels * metersInPixel
    }
}