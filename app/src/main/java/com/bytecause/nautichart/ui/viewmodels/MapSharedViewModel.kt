package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.local.room.tables.SearchPlaceCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class MapSharedViewModel : ViewModel() {

    private val _permissionGranted = MutableStateFlow<Boolean?>(null)
    val permissionGranted: StateFlow<Boolean?> get() = _permissionGranted.asStateFlow()

    private val _tileSource = MutableLiveData(TileSourceFactory.MAPNIK)
    val tileSource: LiveData<OnlineTileSourceBase> get() = _tileSource

    private val _gridOverlayVisible = MutableLiveData<Boolean>()
    val gridOverlayVisible: LiveData<Boolean> get() = _gridOverlayVisible

    private val _placeToFindStateFlow = MutableStateFlow<SearchPlaceCacheEntity?>(null)
    val placeToFindStateFlow get() = _placeToFindStateFlow.asStateFlow()

    private val _showPoiStateFlow = MutableStateFlow<Map<String, List<Long>>?>(null)
    val showPoiStateFlow get() = _showPoiStateFlow.asStateFlow()

    private val _dismissSearchMapDialog = MutableStateFlow<Boolean?>(null)
    val dismissSearchMapDialog get() = _dismissSearchMapDialog.asStateFlow()

    var mapCenter: IGeoPoint? = null
        private set

    var zoomLevel: Double? = null
        private set

    private var intentLatitude: String? = null
    private var intentLongitude: String? = null
    private var intentZoom: Double? = null

    var geoPoint: GeoPoint? = null
        private set

    private val _lastKnownPosition = MutableSharedFlow<GeoPoint?>(1)
    val lastKnownPosition get() = _lastKnownPosition.asSharedFlow()

    fun permissionGranted(bool: Boolean?) {
        _permissionGranted.value = bool
    }

    fun setLastKnownPosition(position: GeoPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            _lastKnownPosition.emit(position)
        }
    }

    fun setPlaceToFind(entity: SearchPlaceCacheEntity?) {
        viewModelScope.launch(Dispatchers.IO) {
            _placeToFindStateFlow.emit(entity)
        }
    }

    fun setPoiToShow(poiMap: Map<String, List<Long>>?) {
        viewModelScope.launch(Dispatchers.IO) {
            _showPoiStateFlow.emit(poiMap)
        }
    }

    fun setDismissSearchMapDialogState(bool: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            _dismissSearchMapDialog.emit(bool)
        }
    }

    fun toggleGridOverlay() {
        _gridOverlayVisible.value = _gridOverlayVisible.value != true
    }

    fun setTile(src: OnlineTileSourceBase?) {
        src ?: return
        _tileSource.value = src
    }

    fun setZoomLevel(zoomLevel: Double) {
        this.zoomLevel = zoomLevel
    }

    fun setCenterPoint(geoPoint: IGeoPoint) {
        mapCenter = geoPoint
    }

    fun setIntentCoordinates(lat: String, lon: String, zoom: Double) {
        intentLatitude = lat
        intentLongitude = lon
        intentZoom = zoom
    }

    fun getIntentCoordinates(): GeoPoint? {
        return if (intentLatitude != null && intentLongitude != null && intentZoom != null) {
            val latToDouble = intentLatitude?.toDouble()
            val lonToDouble = intentLongitude?.toDouble()
            GeoPoint(latToDouble!!, lonToDouble!!)
        } else {
            null
        }
    }

    fun getIntentZoom(): Double? = if (intentZoom != null) intentZoom else null

    fun setGeoPoint(geoPoint: GeoPoint) {
        this.geoPoint = geoPoint
    }
}