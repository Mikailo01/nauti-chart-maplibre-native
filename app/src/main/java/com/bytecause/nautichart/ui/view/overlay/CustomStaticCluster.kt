package com.bytecause.nautichart.ui.view.overlay

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

class CustomStaticCluster(center: GeoPoint) {

    private val mItems: MutableList<CustomMarker> = mutableListOf()
    protected var mCenter: GeoPoint? = null
    protected var mMarker: CustomMarker? = null

    init {
        mCenter = center
    }

    fun getItems(): List<CustomMarker> = mItems
    fun getItem(index: Int): CustomMarker = mItems[index]
    fun getSize(): Int = mItems.size
    fun add(t: CustomMarker?): Boolean {
        t ?: return false
        return mItems.add(t)
    }
    fun getPosition(): GeoPoint? = mCenter
    fun setPosition(center: GeoPoint) {
        mCenter = center
    }
    fun setMarker(marker: CustomMarker?) {
        marker ?: return
        mMarker = marker
    }
    fun getMarker(): CustomMarker? = mMarker
    fun getBoundingBox(): BoundingBox? {
        if (getSize() == 0) return null
        var p = getItem(0).position
        val bb = BoundingBox(p.latitude, p.longitude, p.latitude, p.longitude)
        for (i in 1 until getSize()) {
            p = getItem(i).position
            val minLat = bb.latSouth.coerceAtMost(p.latitude)
            val minLon = bb.lonWest.coerceAtMost(p.longitude)
            val maxLat = bb.latNorth.coerceAtLeast(p.latitude)
            val maxLon = bb.lonEast.coerceAtLeast(p.longitude)
            bb[maxLat, maxLon, minLat] = minLon
        }
        return bb
    }
}