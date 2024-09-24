package com.bytecause.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.presentation.model.PlaceType
import com.bytecause.presentation.model.PointType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng


class MapSharedViewModel : ViewModel() {
    private val _permissionGranted = MutableStateFlow<Boolean?>(null)
    val permissionGranted: StateFlow<Boolean?> get() = _permissionGranted.asStateFlow()

    private val _tileSource = MutableSharedFlow<TileSources?>(1)
    val tileSource: SharedFlow<TileSources?> = _tileSource.asSharedFlow()

    private val _searchPlace = MutableStateFlow<PlaceType?>(null)
    val searchPlace get() = _searchPlace.asStateFlow()

    private val _showPoiStateFlow = MutableStateFlow<Map<String, List<Long>>?>(null)
    val showPoiStateFlow get() = _showPoiStateFlow.asStateFlow()

    private val _isCustomizeDialogVisible = MutableStateFlow(false)
    val isCustomizeDialogVisible = _isCustomizeDialogVisible.asStateFlow()

    private val _geoIntentFlow = MutableSharedFlow<Pair<LatLng, Double>?>(1)
    val geoIntentFlow = _geoIntentFlow.asSharedFlow()

    var cameraPosition: CameraPosition? = null
        private set

    private val _latLngFlow = MutableStateFlow<PointType?>(null)
    val latLngFlow = _latLngFlow.asStateFlow()

    private val _lastKnownPosition = MutableSharedFlow<LatLng?>(1)
    val lastKnownPosition get() = _lastKnownPosition.asSharedFlow()

    private val _showAnchorageAlarmBottomSheet = MutableStateFlow(false)
    val showAnchorageAlarmBottomSheet = _showAnchorageAlarmBottomSheet.asStateFlow()

    fun setShowAnchorageAlarmBottomSheet(boolean: Boolean) {
        viewModelScope.launch {
            _showAnchorageAlarmBottomSheet.emit(boolean)
        }
    }

    fun permissionGranted(bool: Boolean?) {
        _permissionGranted.value = bool
    }

    fun setIsCustomizeDialogVisible(b: Boolean) {
        _isCustomizeDialogVisible.update { b }
    }

    fun setLastKnownPosition(position: LatLng) {
        viewModelScope.launch {
            _lastKnownPosition.emit(position)
        }
    }

    fun setSearchPlace(entity: PlaceType?) {
        _searchPlace.value = entity
    }

    fun setPoiToShow(poiMap: Map<String, List<Long>>?) {
        _showPoiStateFlow.value = poiMap
    }

    fun setTile(src: TileSources) {
        viewModelScope.launch {
            _tileSource.emit(src)
        }
    }

    fun saveCameraPosition(position: CameraPosition) {
        cameraPosition = position
    }

    fun setIntentCoordinates(
        latLng: LatLng,
        zoom: Double,
    ) {
        viewModelScope.launch {
            _geoIntentFlow.emit(latLng to zoom)
        }
    }

    fun resetGeoIntentFlow() {
        viewModelScope.launch {
            _geoIntentFlow.emit(null)
        }
    }

    fun setLatLng(latLng: PointType?) {
        _latLngFlow.update { latLng }
    }
}
