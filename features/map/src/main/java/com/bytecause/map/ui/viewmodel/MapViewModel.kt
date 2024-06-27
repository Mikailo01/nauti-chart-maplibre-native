package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.HarboursEntity
import com.bytecause.data.repository.abstractions.CustomPoiRepository
import com.bytecause.data.repository.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.domain.usecase.FetchVesselsUseCase
import com.bytecause.map.data.repository.HarboursDatabaseRepository
import com.bytecause.map.data.repository.HarboursRepository
import com.bytecause.map.util.MapUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import javax.inject.Inject

// TODO("Refactor")
@HiltViewModel
class MapViewModel
@Inject
constructor(
    // I didn't use UseCase for this, because I will probably replace this API.
    private val harboursRepository: HarboursRepository,
    private val harboursDatabaseRepository: HarboursDatabaseRepository,
    private val poiCacheRepository: PoiCacheRepository,
    private val customPoiRepository: CustomPoiRepository,
    fetchVesselsUseCase: FetchVesselsUseCase,
    private val vesselsDatabaseRepository: VesselsDatabaseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customTileSourcesUseCase: CustomTileSourcesUseCase,
) : ViewModel() {

    private var _harboursVisible = MutableLiveData(false)
    val harboursVisible: LiveData<Boolean> get() = _harboursVisible

    private var _locationButtonStateFlow = MutableStateFlow<Int?>(null)
    val locationButtonStateFlow get() = _locationButtonStateFlow.asStateFlow()

    private val vesselsBbox = MutableSharedFlow<LatLngBounds>(1)
    private val poisBbox = MutableStateFlow<LatLngBounds?>(null)

    private val _selectedFeatureIdFlow = MutableStateFlow<com.bytecause.map.ui.FeatureType>(
        com.bytecause.map.ui.FeatureType.CustomPoi(null)
    )
    val selectedFeatureIdFlow = _selectedFeatureIdFlow.asStateFlow()

    var isMeasuring = false
        private set

    private val _measurePointsSharedFlow = MutableSharedFlow<List<LatLng>>(replay = 1)
    val measurePointsSharedFlow get() = _measurePointsSharedFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val vesselsFlow: Flow<List<VesselModel>> = vesselsBbox.mapLatest {
        fetchVesselsUseCase().firstOrNull()?.data?.let { vessels ->
            filterVisibleVessels(
                latLngBounds = it,
                vesselsList = vessels
            )
        } ?: emptyList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val poisFlow: Flow<List<PoiCacheModel>?> = poisBbox.flatMapLatest {
        it?.let {
            loadPoiCacheByBoundingBox(it)
        } ?: run {
            flowOf(null)
        }
    }

    fun setSelectedFeatureId(featureType: com.bytecause.map.ui.FeatureType) {
        _selectedFeatureIdFlow.update { featureType }
    }

    fun updateVesselBbox(bbox: LatLngBounds) {
        viewModelScope.launch {
            vesselsBbox.emit(bbox)
        }
    }

    fun updatePoiBbox(bounds: LatLngBounds?) {
        poisBbox.update { bounds }
    }

    fun setIsMeasuring(boolean: Boolean) {
        isMeasuring = boolean
    }

    fun addMeasurePoint(latLng: LatLng) {
        viewModelScope.launch {
            _measurePointsSharedFlow.emit(
                _measurePointsSharedFlow.replayCache.lastOrNull()
                    ?.plus(latLng) ?: listOf(latLng),
            )
        }
    }

    fun removeMeasurePoint(latLng: LatLng) {
        viewModelScope.launch {
            _measurePointsSharedFlow.emit(_measurePointsSharedFlow.replayCache.last() - latLng)
        }
    }

    fun clearMeasurePoints(entireClear: Boolean = false) {
        viewModelScope.launch {
            _measurePointsSharedFlow.emit(
                if (entireClear) {
                    listOf()
                } else {
                    listOf(_measurePointsSharedFlow.replayCache[0].first())
                },
            )
        }
    }

    fun calculateDistance(latLngList: List<LatLng>): Double {
        var distance = 0.0
        for (index in latLngList.indices) {
            if (index == latLngList.lastIndex) return distance
            distance += latLngList[index].distanceTo(latLngList[index + 1])
        }
        return distance
    }

    fun filterPoiByBoundingBox(latLngBounds: LatLngBounds): Flow<List<PoiCacheModel>> =
        poiCacheRepository.loadCachedResults()
            .map { list ->
                list.filter {
                    MapUtil.isPositionInBoundingBox(
                        LatLng(
                            it.latitude,
                            it.longitude,
                        ),
                        latLngBounds,
                    )
                }
            }

    fun searchVesselById(id: Int): Flow<VesselInfoModel> =
        vesselsDatabaseRepository.searchVesselById(id)

    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheModel>> =
        poiCacheRepository.searchInCache(placeIds)

    val loadAllCustomPoi: Flow<List<CustomPoiEntity>> = customPoiRepository.loadAllCustomPoi()

    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity> =
        customPoiRepository.searchCustomPoiById(id)

    private fun loadPoiCacheByBoundingBox(latLngBounds: LatLngBounds): Flow<List<PoiCacheModel>> =
        poiCacheRepository.loadPoiCacheByBoundingBox(
            minLat = latLngBounds.latitudeSouth,
            maxLat = latLngBounds.latitudeNorth,
            minLon = latLngBounds.longitudeWest,
            maxLon = latLngBounds.longitudeEast
        )

    val loadAllHarbours: Flow<List<HarboursEntity>> = harboursDatabaseRepository.loadAllHarbours
    val isHarboursDatabaseEmpty: Flow<Boolean> = harboursDatabaseRepository.isHarboursDatabaseEmpty

    fun isHarbourIdInDatabase(idList: List<Int>) =
        harboursDatabaseRepository.isHarbourIdInDatabase(idList)

    fun searchHarbourById(id: Int): Flow<HarboursEntity> =
        harboursDatabaseRepository.searchHarbourById(id)

    fun addHarbours(entity: List<HarboursEntity>) {
        viewModelScope.launch {
            harboursDatabaseRepository.insertAllHarbours(entity)
        }
    }

    fun setLocationButtonState(v: Int) {
        viewModelScope.launch {
            _locationButtonStateFlow.emit(v)
        }
    }

    fun toggleHarboursLocations() {
        _harboursVisible.value = _harboursVisible.value != true
    }

    private fun filterVisibleVessels(
        latLngBounds: LatLngBounds,
        vesselsList: List<VesselModel>,
    ): List<VesselModel> =
        vesselsList.filter { vessel ->
            val latLng =
                LatLng(
                    latitude = vessel.latitude,
                    longitude = vessel.longitude,
                )
            MapUtil.isPositionInBoundingBox(latLng, latLngBounds)
        }

    private fun getCachedTileSourceType(tileSourceName: String?): Flow<TileSources?> =
        flow {
            tileSourceName ?: emit(null)

            emit(
                when (tileSourceName) {
                    // Default tile providers
                    com.bytecause.domain.tilesources.TileSourceId.MAPNIK_RASTER_SOURCE_ID.name -> DefaultTileSources.MAPNIK
                    com.bytecause.domain.tilesources.TileSourceId.SATELLITE_RASTER_SOURCE_ID.name -> DefaultTileSources.SATELLITE
                    com.bytecause.domain.tilesources.TileSourceId.OPEN_TOPO_MAP_SOURCE_ID.name -> DefaultTileSources.OPEN_TOPO
                    else -> {
                        customTileSourcesUseCase().firstOrNull()?.let {
                            it.values.map {
                                it.find {
                                    when (val type = it.type) {
                                        is CustomTileProviderType.Raster.Online -> {
                                            type.name
                                        }

                                        is CustomTileProviderType.Raster.Offline -> {
                                            type.name
                                        }

                                        is CustomTileProviderType.Vector.Offline -> {
                                            type.name
                                        }
                                    } == tileSourceName
                                }
                            }.firstOrNull()?.run {
                                (type as? CustomTileProviderType.Raster.Online)?.run {
                                    TileSources.Raster.Custom.Online(
                                        id = name,
                                        url = url,
                                        tileSize = tileSize,
                                        minZoom = minZoom.toFloat(),
                                        maxZoom = maxZoom.toFloat(),
                                    )
                                } ?: run {
                                    (type as? CustomTileProviderType.Raster.Offline)?.run {
                                        TileSources.Raster.Custom.Offline(
                                            id = name,
                                            minZoom = minZoom.toFloat(),
                                            maxZoom = maxZoom.toFloat(),
                                            tileSize = tileSize,
                                            filePath = filePath
                                        )
                                    } ?: run { null }
                                }
                            }
                        }
                    }
                }
            )
        }

    // User Preferences DataStore operations.
    fun getFirstRunFlag() = userPreferencesRepository.getFirstRunFlag()

    fun saveUserLocation(position: LatLng) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserPosition(position)
        }
    }

    fun getUserLocation() = userPreferencesRepository.getUserPosition()

    fun getCachedTileSourceType(): Flow<TileSources?> =
        flow {
            userPreferencesRepository.getCachedTileSource().firstOrNull()?.let {
                emit(getCachedTileSourceType(it).firstOrNull())
            }
        }
}
