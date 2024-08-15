package com.bytecause.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.domain.tilesources.TileSources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _tileSource = MutableStateFlow<TileSources?>(null)
    val tileSource: StateFlow<TileSources?> = _tileSource.asStateFlow()

    private val _placeToFindStateFlow = MutableStateFlow<SearchPlaceCacheEntity?>(null)
    val placeToFindStateFlow get() = _placeToFindStateFlow.asStateFlow()

    private val _showPoiStateFlow = MutableStateFlow<Map<String, List<Long>>?>(null)
    val showPoiStateFlow get() = _showPoiStateFlow.asStateFlow()

    private val _dismissSearchMapDialog = MutableStateFlow<Boolean?>(null)
    val dismissSearchMapDialog get() = _dismissSearchMapDialog.asStateFlow()

    private val _isCustomizeDialogVisible = MutableStateFlow(false)
    val isCustomizeDialogVisible = _isCustomizeDialogVisible.asStateFlow()

    private val _geoIntentFlow = MutableSharedFlow<Pair<LatLng, Double>?>(1)
    val geoIntentFlow = _geoIntentFlow.asSharedFlow()

    var cameraPosition: CameraPosition? = null
        private set

    private val _latLngFlow = MutableStateFlow<LatLng?>(null)
    val latLngFlow = _latLngFlow.asStateFlow()

    private val _lastKnownPosition = MutableSharedFlow<LatLng?>(1)
    val lastKnownPosition get() = _lastKnownPosition.asSharedFlow()

    fun permissionGranted(bool: Boolean?) {
        _permissionGranted.value = bool
    }

    fun setIsCustomizeDialogVisible(b: Boolean) {
        _isCustomizeDialogVisible.update { b }
    }

    fun setLastKnownPosition(position: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            _lastKnownPosition.emit(position)
        }
    }

    fun setPlaceToFind(entity: SearchPlaceCacheEntity?) {
        _placeToFindStateFlow.value = entity
    }

    fun setPoiToShow(poiMap: Map<String, List<Long>>?) {
        _showPoiStateFlow.value = poiMap
    }

    fun setDismissSearchMapDialogState(bool: Boolean?) {
        _dismissSearchMapDialog.value = bool
    }

    fun setTile(src: TileSources) {
        _tileSource.update { src }
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

    fun setLatLng(latLng: LatLng?) {
        _latLngFlow.update {
            latLng
        }
    }
}
