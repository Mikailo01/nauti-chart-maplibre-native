package com.bytecause.nautichart.ui.view.overlay

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.data.local.room.tables.HarboursEntity
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import com.bytecause.nautichart.interfaces.MapFragmentInterface
import com.bytecause.nautichart.ui.util.DrawableUtil
import com.bytecause.nautichart.ui.util.modifyDrawableScale
import com.bytecause.nautichart.ui.view.custom.CustomMapView
import com.bytecause.nautichart.ui.view.fragment.addOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.kml.ColorStyle
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.bonuspack.kml.LineStyle
import org.osmdroid.bonuspack.kml.Style
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.math.round

class OverlayHelper(
    private val mapView: CustomMapView,
    private val mapFragmentInterface: MapFragmentInterface
) {

    private var landBoundariesOverlay: FolderOverlay? = null
    private var targetOverlay: TargetOverlay? = null

    private var harboursLoadingJob: Job? = null

    var harboursClusterer: CustomRadiusMarkerClusterer? = null
        private set

    var customPoiMarkerList: List<CustomMarker> = listOf()
        private set

    var isMeasuring: Boolean = false
        private set

    var itemizedMarkerIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
        private set

    var itemizedMarkerIconList: List<ItemizedIconOverlay<OverlayItem>> = listOf()
        private set

    var poiOverlayItemList: List<OverlayItem> = listOf()
        private set

    var itemizedPoiIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
        private set

    var geoPointList: List<GeoPoint> = listOf()
        private set


    private var boundaryLines: Polyline? = null
    private var _boundaryLinePaths: List<Polyline> = listOf()
    val boundaryLinePaths get() = _boundaryLinePaths

    private var distanceLine: Polyline? = null
    var distanceLinePaths: List<Polyline> = listOf()
        private set

    fun setIsMeasuring(boolean: Boolean) {
        this.isMeasuring = boolean
    }

    fun setGeoPointList(list: List<GeoPoint>) {
        this.geoPointList = list
    }

    fun clearGeoPointList() {
        geoPointList = listOf()
    }

    fun setItemizedIconArray(array: List<ItemizedIconOverlay<OverlayItem>>) {
        this.itemizedMarkerIconList = array
    }

    fun drawPolygon(coordinates: List<GeoPoint>) {
        boundaryLinePaths.takeIf { it.isNotEmpty() }?.let { mapView.overlays.removeAll(it) }
        boundaryLines = Polyline()
        boundaryLines?.let {
            for (i in 0 until coordinates.size - 1) {
                it.addPoint(coordinates[i])
                it.addPoint(coordinates[i + 1])
            }
            mapView.overlays.addOverlay(it, mapFragmentInterface)
            _boundaryLinePaths = _boundaryLinePaths + it
            mapView.invalidate()
        }
    }

    fun addLandBoundariesLayer() {
        val jsonString: String?
        try {
            val jsonStream: InputStream = mapView.context.assets.open("country_boundaries.geojson")
            val size = jsonStream.available()
            val buffer = ByteArray(size)
            jsonStream.use { it.read(buffer) }
            jsonString = String(buffer, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val kmlDocument = KmlDocument()
        kmlDocument.parseGeoJSON(jsonString)
        val style = Style().apply {
            mPolyStyle = ColorStyle().apply {
                mColor = ContextCompat.getColor(mapView.context, R.color.transparent)
                mLineStyle = LineStyle(ContextCompat.getColor(mapView.context, R.color.black), 6f)
            }
        }
        landBoundariesOverlay =
            kmlDocument.mKmlRoot.buildOverlay(mapView, style, null, kmlDocument) as FolderOverlay
        mapView.overlays.addOverlay(landBoundariesOverlay, mapFragmentInterface)
    }

    /*fun addPortsLayer() {
        val jsonString: String?
        try {
            val jsonStream: InputStream = mapView.context.assets.open("export.geojson")
            val size = jsonStream.available()
            val buffer = ByteArray(size)
            jsonStream.use { it.read(buffer) }
            jsonString = String(buffer, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val drawable = ContextCompat.getDrawable(mapView.context, R.drawable.harbour_marker_icon)
        val kmlDocument = KmlDocument().apply {
            parseGeoJSON(jsonString)
        }

        for (x in kmlDocument.mKmlRoot.mItems) {
            val marker: CustomMarker = CustomMarker(mapView).apply {
                setOnMarkerClickListener(mapFragmentInterface)
                position = x.boundingBox.centerWithDateLine
                title = x.getExtendedData("Name")
                subDescription = x.getExtendedData("Country")
                icon = drawable
                setMarkerType(CustomMarker.CustomMarkerType.HarbourMarker)
            }
            _harboursClusterer?.add(marker)
        }

        harboursClusterer?.invalidate()
        mapView.overlays.add(harboursClusterer)
    }*/

    fun addTargetOverlay() {
        val targetDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.target_icon)
        val targetBitmap = BitmapDrawable(mapView.context.resources, targetDrawable?.toBitmap())
        targetOverlay = TargetOverlay(targetBitmap)

        mapView.overlays.addOverlay(targetOverlay, mapFragmentInterface)
        mapView.invalidate()
    }

    fun getLandBoundariesOverlay(): FolderOverlay? {
        return landBoundariesOverlay
    }

    fun getTargetOverlay(): TargetOverlay? {
        return targetOverlay
    }

    fun overwriteDistanceLinePaths(paths: List<Polyline>) {
        distanceLinePaths = paths
    }

    fun clearDistanceLinePaths() {
        distanceLinePaths = listOf()
    }

    fun addMeasurePoint(geoPoint: GeoPoint) {
        if (!this.isMeasuring) return

        val overlayItem = OverlayItem("Marker", "Location Marker", geoPoint)
        val markerDrawable: Drawable =
            ContextCompat.getDrawable(mapView.context, R.drawable.pin_icon) ?: return

        if (geoPointList.size == 1) {
            this.itemizedMarkerIconOverlay?.removeAllItems()
            itemizedMarkerIconList = emptyList()
        }

        overlayItem.setMarker(markerDrawable)

        this.itemizedMarkerIconOverlay = ItemizedIconOverlay(
            mapView.context, mutableListOf(overlayItem), null
        ).apply {
            itemizedMarkerIconList = itemizedMarkerIconList + this
            if (!this@OverlayHelper.isMeasuring) geoPointList = listOf(geoPoint)

            mapView.overlays.addOverlay(this, mapFragmentInterface)
            mapView.invalidate()
        }
    }

    suspend fun addPoiToMap(poiList: List<PoiCacheEntity>) {
        withContext(Dispatchers.Default) {
            itemizedPoiIconOverlay?.also {
                mapView.overlays.remove(it)
                poiOverlayItemList = listOf()
                itemizedPoiIconOverlay = null
            }

            for (poi in poiList) {
                val overlayItem = OverlayItem(
                    poi.category,
                    poi.tags.entries.joinToString(),
                    GeoPoint(poi.latitude, poi.longitude)
                )

                val layerDrawable: LayerDrawable = (ContextCompat.getDrawable(
                    mapView.context,
                    R.drawable.universal_poi_marker_icon
                ) as LayerDrawable).apply {
                    setTint(
                        ContextCompat.getColor(
                            mapView.context,
                            DrawableUtil.assignDrawableColorToPoiCategory(poi.category)
                        )
                    )
                    setDrawableByLayerId(
                        R.id.top_layer,
                        when (poi.drawableResourceName) {
                            "" -> ContextCompat.getDrawable(mapView.context, R.drawable.circle)
                            else -> ContextCompat.getDrawable(
                                mapView.context,
                                mapView.context.resources.getIdentifier(
                                    poi.drawableResourceName,
                                    "drawable",
                                    mapView.context.packageName
                                )
                            )
                        }
                    )
                }
                overlayItem.setMarker(layerDrawable)

                poiOverlayItemList = poiOverlayItemList + overlayItem
            }

            itemizedPoiIconOverlay = ItemizedIconOverlay(
                mapView.context,
                poiOverlayItemList,
                null
            )
            //mapView.overlays.add(itemizedPoiIconOverlay)
            mapView.overlays.addOverlay(itemizedPoiIconOverlay, mapFragmentInterface)
            mapView.postInvalidate()
        }
    }

    fun resetItemizedPoiIconOverlay() {
        this.itemizedPoiIconOverlay = null
    }

    fun addMarkerToMapAndShowBottomSheet(geoPoint: GeoPoint) {
        if (mapView.overlayManager.contains(this.itemizedMarkerIconOverlay)) mapView.overlayManager.remove(
            this.itemizedMarkerIconOverlay
        )
        setGeoPointList(listOf(geoPoint))

        val overlayItem = OverlayItem("Marker", "Location Marker", geoPoint)

        overlayItem.setMarker(ContextCompat.getDrawable(mapView.context, R.drawable.map_marker))

        this.itemizedMarkerIconOverlay = ItemizedIconOverlay(
            mapView.context, mutableListOf(overlayItem), null
        ).apply {
            if (itemizedMarkerIconList.isNotEmpty()) {
                itemizedMarkerIconList = emptyList()
                itemizedMarkerIconList = listOf(this)
            }
            mapFragmentInterface.markerToMapAdded(geoPoint)
            mapView.overlays.addOverlay(this, mapFragmentInterface)
            mapView.invalidate()
        }
    }

    fun drawDistancePaths(geopointList: List<GeoPoint>) {
        if (geopointList.size < 2) return

        distanceLine = Polyline()
        distanceLine?.let {
            for (i in 0 until geopointList.size - 1) {
                it.addPoint(geopointList[i])
                it.addPoint(geopointList[i + 1])
            }
            mapView.overlays.addOverlay(it, mapFragmentInterface)
            distanceLinePaths = distanceLinePaths + it
            calculateDistance()
            mapView.invalidate()
        }
    }

    fun calculateDistance() {
        var totalDistance = 0.0
        for (distance in distanceLinePaths) {
            totalDistance += distance.distance
        }
        val distanceTextViewText =
            if (totalDistance > 1000) ((round(totalDistance / 1000 * 10) / 10).toString()) + " KM" else round(
                totalDistance
            ).toInt().toString() + " M"
        mapFragmentInterface.updateDistanceTextView(distanceTextViewText)
    }

    fun initHarboursClusterer() {
        harboursClusterer ?: run {
            harboursClusterer = CustomRadiusMarkerClusterer(mapView.context).apply {
                setRadius(160)
                setMaxClusteringZoomLevel(7.0)
                val bitmap =
                    ContextCompat.getDrawable(mapView.context, R.drawable.harbours_clusterer_icon)
                        ?.toBitmap()
                setIcon(bitmap)
                setTextColor(ContextCompat.getColor(mapView.context, R.color.black))
                mTextAnchorV = 0.25f
                mTextAnchorU = 0.35f
            }
            mapView.overlayManager.addOverlay(harboursClusterer, mapFragmentInterface)
        }
    }

    fun deinitHarboursClusterer() {
        harboursClusterer = null
    }

    fun drawHarbourMarkerOnMap(entity: List<HarboursEntity>?, selectedMarkerId: String? = null) {
        if (entity.isNullOrEmpty()) return
        harboursLoadingJob?.cancel()
        harboursLoadingJob = CoroutineScope(Dispatchers.Default).launch {
            harboursClusterer?.let {
                if (it.getItems().isNotEmpty()) it.clearItems()

                entity.forEach { harbourInfo ->
                    val geoPoint = GeoPoint(harbourInfo.latitude, harbourInfo.longitude)
                    val iconDrawableId = when (harbourInfo.type) {
                        "-1" -> R.drawable.marina
                        else -> R.drawable.harbour_marker_icon
                    }
                    CustomMarker(mapView).apply {
                        setOnMarkerClickListener(mapFragmentInterface)
                        id = harbourInfo.harborId.toString()
                        if (selectedMarkerId == id) isClicked(true)
                        icon = ContextCompat.getDrawable(mapView.context, iconDrawableId)
                        position = geoPoint
                        setDrawableId(iconDrawableId)
                        //setMarkerType(CustomMarker.CustomMarkerType.HarbourMarker)
                        it.add(this)
                    }
                }
                it.invalidate()
                mapView.postInvalidate()
            }
        }
    }

    fun cancelHarboursLoadingJob() {
        harboursLoadingJob?.cancel()
        harboursLoadingJob = null
    }

    @SuppressLint("ResourceType")
    fun drawCustomPoiOnMap(entity: List<CustomPoiEntity>?, selectedMarkerId: String? = null) {
        if (entity.isNullOrEmpty()) {
            if (customPoiMarkerList.isNotEmpty()) {
                mapView.overlayManager.removeAll(customPoiMarkerList)
                customPoiMarkerList = listOf()
                mapView.invalidate()
            }
            return
        } else if (customPoiMarkerList.isNotEmpty()) {
            mapView.overlayManager.removeAll(customPoiMarkerList)
            customPoiMarkerList = listOf()
        }

        entity.forEach { customPoi ->
            val geoPoint = GeoPoint(customPoi.latitude, customPoi.longitude)
            CustomMarker(mapView).apply {
                setOnMarkerClickListener(mapFragmentInterface)
                id = customPoi.poiId.toString()
                if (selectedMarkerId == id) isClicked(true)
                icon = mapView.context.modifyDrawableScale(
                    ContextCompat.getDrawable(
                        mapView.context,
                        mapView.context.resources.getIdentifier(
                            customPoi.drawableResourceName,
                            "drawable",
                            mapView.context.packageName
                        )
                    ), 0.5f
                ) ?: ContextCompat.getDrawable(mapView.context, R.drawable.map_marker)
                position = geoPoint
                setMarkerType(CustomMarker.CustomMarkerType.CustomPoi)
                customPoiMarkerList = customPoiMarkerList + this
                mapView.overlayManager.addOverlay(this, mapFragmentInterface)
            }
        }
        mapView.invalidate()
    }
}