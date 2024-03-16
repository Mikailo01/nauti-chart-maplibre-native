package com.bytecause.nautichart.ui.view.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.view.MotionEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

abstract class CustomMarkerClusterer : Overlay() {

    protected val FORCE_CLUSTERING = -1

    protected val mItems: MutableList<CustomMarker> = mutableListOf()
    protected var mPoint = Point()
    protected var mClusters: MutableList<CustomStaticCluster> = mutableListOf()
    protected var mLastZoomLevel = 0
    protected var mClusterIcon: Bitmap? = null
    protected var mName: String? = null
    protected var mDescription: String? = null

    // abstract methods:

    // abstract methods:
    /** clustering algorithm  */
    abstract fun clusterer(mapView: MapView?): MutableList<CustomStaticCluster>

    /** Build the marker for a cluster.  */
    abstract fun buildClusterMarker(cluster: CustomStaticCluster?, mapView: MapView?): CustomMarker?

    /** build clusters markers to be used at next draw  */
    abstract fun renderer(
        clusters: MutableList<CustomStaticCluster>?,
        canvas: Canvas?,
        mapView: MapView?
    )

    init {
        mLastZoomLevel = FORCE_CLUSTERING
    }

    open fun setName(name: String?) {
        mName = name
    }

    open fun getName(): String? {
        return mName
    }

    open fun setDescription(description: String) {
        mDescription = description
    }

    open fun getDescription(): String? {
        return mDescription
    }

    /** Set the cluster icon to be drawn when a cluster contains more than 1 marker.
     * If not set, default will be the default osmdroid marker icon (which is really inappropriate as a cluster icon).  */
    open fun setIcon(icon: Bitmap?) {
        mClusterIcon = icon
    }

    /** Add the Marker.
     * Important: Markers added in a MarkerClusterer should not be added in the map overlays.  */
    open fun add(marker: CustomMarker) {
        mItems.add(marker)
    }

    /** Force a rebuild of clusters at next draw, even without a zooming action.
     * Should be done when you changed the content of a MarkerClusterer.  */
    open fun invalidate() {
        mLastZoomLevel = FORCE_CLUSTERING
    }

    /** @return the Marker at id (starting at 0)
     */
    open fun getItem(id: Int): CustomMarker? {
        return mItems[id]
    }

    /** @return the list of Markers.
     */
    open fun getItems(): List<CustomMarker>? {
        return mItems
    }

    protected open fun hideInfoWindows() {
        for (m in mItems) {
            if (m.isInfoWindowShown) m.closeInfoWindow()
        }
    }

    override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
        if (shadow || mapView == null) return
        //if zoom has changed and mapView is now stable, rebuild clusters:
        val zoomLevel = mapView.zoomLevel
        if (zoomLevel != mLastZoomLevel && !mapView.isAnimating) {
            hideInfoWindows()
            mClusters = clusterer(mapView)
            renderer(mClusters, canvas, mapView)
            mLastZoomLevel = zoomLevel
        }

        for (cluster in mClusters) {
            cluster.getMarker()?.draw(canvas, mapView.projection)
        }
    }

    open fun reversedClusters(): Iterable<CustomStaticCluster> {
        return object : Iterable<CustomStaticCluster> {
            override fun iterator(): MutableIterator<CustomStaticCluster> {
                val i = mClusters.listIterator(mClusters.size)
                return object : MutableIterator<CustomStaticCluster> {
                    override fun hasNext(): Boolean {
                        return i.hasPrevious()
                    }

                    override fun next(): CustomStaticCluster {
                        return i.previous()
                    }

                    override fun remove() {
                        i.remove()
                    }
                }
            }
        }
    }

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        for (cluster in reversedClusters()) {
            cluster.getMarker()?.let {
                if (it.onSingleTapConfirmed(event, mapView)) return true
            }
        }
        return false
    }

    override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
        for (cluster in reversedClusters()) {
            cluster.getItems()
            cluster.getMarker()?.let {
                if (it.onLongPress(event, mapView)) return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
        for (cluster in reversedClusters()) {
            cluster.getMarker()?.let {
                if (it.onTouchEvent(event, mapView)) return true
            }
        }
        return false
    }

    override fun getBounds(): BoundingBox? {
        if (mItems.size == 0) return null
        var minLat = Double.MAX_VALUE
        var minLon = Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE
        var maxLon = -Double.MAX_VALUE
        for (item in mItems) {
            val latitude = item.position.latitude
            val longitude = item.position.longitude
            minLat = minLat.coerceAtMost(latitude)
            minLon = minLon.coerceAtMost(longitude)
            maxLat = maxLat.coerceAtLeast(latitude)
            maxLon = maxLon.coerceAtLeast(longitude)
        }
        return BoundingBox(maxLat, maxLon, minLat, minLon)
    }
}