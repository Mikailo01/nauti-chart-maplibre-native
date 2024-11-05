package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.local.room.tables.AnchoragesEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.repository.abstractions.AnchorageAlarmPreferencesRepository
import com.bytecause.data.repository.abstractions.AnchorageMovementTrackRepository
import com.bytecause.data.repository.abstractions.CustomPoiRepository
import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.domain.usecase.GetHarboursUseCase
import com.bytecause.domain.usecase.GetVesselsUseCase
import com.bytecause.map.data.repository.abstraction.AnchorageHistoryRepository
import com.bytecause.map.data.repository.abstraction.AnchoragesRepository
import com.bytecause.map.data.repository.abstraction.TrackRouteRepository
import com.bytecause.map.services.AnchorageAlarmService
import com.bytecause.map.ui.effect.TrackRouteBottomSheetEffect
import com.bytecause.map.ui.event.TrackRouteBottomSheetEvent
import com.bytecause.map.ui.mappers.asAnchorageHistory
import com.bytecause.map.ui.mappers.asAnchorageHistoryUiModel
import com.bytecause.map.ui.mappers.asHarbourUiModel
import com.bytecause.map.ui.mappers.asPoiUiModel
import com.bytecause.map.ui.mappers.asPoiUiModelWithTags
import com.bytecause.map.ui.mappers.asRouteRecordUiModel
import com.bytecause.map.ui.mappers.asTrackedRouteItem
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.AnchorageRepositionType
import com.bytecause.map.ui.model.HarboursUiModel
import com.bytecause.map.ui.model.MeasureUnit
import com.bytecause.map.ui.model.MetersUnitConvertConstants
import com.bytecause.map.ui.model.PoiUiModel
import com.bytecause.map.ui.model.PoiUiModelWithTags
import com.bytecause.map.ui.model.RouteRecordUiModel
import com.bytecause.map.ui.model.SearchBoxTextType
import com.bytecause.map.ui.model.TrackedRouteItem
import com.bytecause.map.ui.state.TrackRouteChooseFilterState
import com.bytecause.map.ui.state.TrackRouteChooseSorterState
import com.bytecause.map.ui.state.TrackRouteMainContentState
import com.bytecause.map.util.MapUtil
import com.bytecause.util.extensions.toFirstDecimal
import com.bytecause.util.mappers.asLatLng
import com.bytecause.util.mappers.asLatLngModel
import com.bytecause.util.mappers.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Point
import javax.inject.Inject
import kotlin.math.round


@HiltViewModel
class MapViewModel
@Inject
constructor(
    private val harboursDatabaseRepository: dagger.Lazy<HarboursDatabaseRepository>,
    private val poiCacheRepository: dagger.Lazy<PoiCacheRepository>,
    private val radiusPoiCacheRepository: dagger.Lazy<RadiusPoiCacheRepository>,
    private val customPoiRepository: dagger.Lazy<CustomPoiRepository>,
    getVesselsUseCase: dagger.Lazy<GetVesselsUseCase>,
    getHarboursUseCase: dagger.Lazy<GetHarboursUseCase>,
    private val vesselsDatabaseRepository: dagger.Lazy<VesselsDatabaseRepository>,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customTileSourcesUseCase: CustomTileSourcesUseCase,
    private val anchoragesRepository: dagger.Lazy<AnchoragesRepository>,
    anchorageAlarmPreferencesRepository: AnchorageAlarmPreferencesRepository,
    anchorageMovementTrackRepository: AnchorageMovementTrackRepository,
    private val anchorageHistoryRepository: dagger.Lazy<AnchorageHistoryRepository>,
    private val trackRouteRepository: dagger.Lazy<TrackRouteRepository>
) : ViewModel() {

    private val _locationButtonStateFlow = MutableStateFlow<Int?>(null)
    val locationButtonStateFlow get() = _locationButtonStateFlow.asStateFlow()

    private val _trackRouteMainContentState = MutableStateFlow(TrackRouteMainContentState())
    val trackRouteMainContentState: StateFlow<TrackRouteMainContentState> =
        _trackRouteMainContentState

    private val _trackRouteChooseSorterState = MutableStateFlow(TrackRouteChooseSorterState())
    val trackRouteChooseSorterState: StateFlow<TrackRouteChooseSorterState> =
        _trackRouteChooseSorterState

    private val _trackRouteChooseFilterState = MutableStateFlow(TrackRouteChooseFilterState())
    val trackRouteChooseFilterState: StateFlow<TrackRouteChooseFilterState> =
        _trackRouteChooseFilterState

    private val _trackRouteEffect =
        Channel<TrackRouteBottomSheetEffect>(capacity = Channel.CONFLATED)
    val trackRouteEffect = _trackRouteEffect.receiveAsFlow()

    private val vesselsBbox = Channel<LatLngBounds>(Channel.CONFLATED)
    private val harboursBbox = Channel<LatLngBounds>(Channel.CONFLATED)
    private val anchoragesBbox = Channel<LatLngBounds>(Channel.CONFLATED)
    private val poisBbox = MutableStateFlow<LatLngBounds?>(null)

    private val _selectedFeatureIdFlow = MutableStateFlow<com.bytecause.map.ui.FeatureType?>(null)
    val selectedFeatureIdFlow = _selectedFeatureIdFlow.asStateFlow()

    private val _searchBoxTextPlaceholder: MutableStateFlow<List<SearchBoxTextType>> =
        MutableStateFlow(emptyList())
    val searchBoxTextPlaceholder: StateFlow<List<SearchBoxTextType>> =
        _searchBoxTextPlaceholder.asStateFlow()

    private val _routeRecord = MutableStateFlow<RouteRecordUiModel?>(null)
    val routeRecord: StateFlow<RouteRecordUiModel?> = _routeRecord

    val isAisActivated: StateFlow<Boolean> = userPreferencesRepository.getIsAisActivated()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val areHarboursVisible: StateFlow<Boolean> = userPreferencesRepository.getAreHarboursVisible()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val areAnchoragesVisible: StateFlow<Boolean> =
        anchorageAlarmPreferencesRepository.getAnchorageLocationsVisible()
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val getTrackedRecords: StateFlow<List<TrackedRouteItem>> =
        trackRouteRepository.get().getRecords()
            .map { originalList -> mapList(originalList) { it.asTrackedRouteItem() } }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val trackedPoints: Flow<List<Point>> =
        combine(
            anchorageAlarmPreferencesRepository.getTrackMovementState(),
            anchorageMovementTrackRepository.getTracks()
        ) { shouldTrack, tracks ->
            if (shouldTrack && AnchorageAlarmService.runningAnchorageAlarm.value.isRunning) {
                mapList(tracks) { Point.fromLngLat(it.longitude, it.latitude) }
            } else emptyList()
        }

    private val _isAnchorageRepositionEnabled = MutableStateFlow(false)
    val isAnchorageRepositionEnabled: StateFlow<Boolean> = _isAnchorageRepositionEnabled

    private val _expandedAnchorageRepositionType = MutableStateFlow<AnchorageRepositionType?>(null)
    val expandedAnchorageRepositionType: StateFlow<AnchorageRepositionType?> =
        _expandedAnchorageRepositionType

    var isMeasuring = false
        private set

    private val _anchorageCenterPoint = MutableStateFlow<LatLng?>(null)
    val anchorageCenterPoint: StateFlow<LatLng?> = _anchorageCenterPoint

    private val _measurePointsSharedFlow = MutableSharedFlow<List<LatLng>>(replay = 1)
    val measurePointsSharedFlow get() = _measurePointsSharedFlow.asSharedFlow()

    val vesselsFlow: Flow<List<VesselModel>> =
        combine(
            isAisActivated,
            vesselsBbox.receiveAsFlow()
        ) { isActivated, bbox ->

            if (isActivated) {
                getVesselsUseCase.get().invoke().firstOrNull()?.let { result ->
                    val vessels = result.data

                    vessels?.let {
                        filterVisible(bbox, it)
                    }

                } ?: emptyList()
            } else emptyList()
        }

    val harboursFlow: Flow<List<HarboursUiModel>> =
        combine(
            areHarboursVisible,
            harboursBbox.receiveAsFlow()
        ) { harboursVisible, bbox ->

            if (harboursVisible) {
                getHarboursUseCase.get().invoke().firstOrNull()?.let { result ->
                    val harbours = result.data

                    harbours?.let {
                        filterVisible(bbox, it.map { it.asHarbourUiModel() })
                    }
                } ?: emptyList()
            } else emptyList()
        }

    val anchoragesFlow: Flow<List<AnchoragesEntity>> =
        combine(
            areAnchoragesVisible,
            anchoragesBbox.receiveAsFlow()
        ) { anchoragesVisible, bbox ->

            if (anchoragesVisible) {
                bbox.run {
                    getAnchoragesByBbox(
                        minLat = latitudeSouth,
                        maxLat = latitudeNorth,
                        minLon = longitudeWest,
                        maxLon = longitudeEast
                    ).firstOrNull() ?: emptyList()
                }
            } else emptyList()
        }

    private fun getAnchoragesByBbox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<AnchoragesEntity>> =
        anchoragesRepository.get().getByBoundingBox(
            minLat = minLat,
            maxLat = maxLat,
            minLon = minLon,
            maxLon = maxLon
        )

    fun insertTextIntoSearchBoxTextPlaceholder(text: SearchBoxTextType) {
        viewModelScope.launch {
            when (text) {
                is SearchBoxTextType.PoiName -> {
                    _searchBoxTextPlaceholder.emit(searchBoxTextPlaceholder.value.filterIsInstance<SearchBoxTextType.Coordinates>() + text)
                }

                is SearchBoxTextType.Coordinates -> {
                    _searchBoxTextPlaceholder.emit(searchBoxTextPlaceholder.value.filterIsInstance<SearchBoxTextType.PoiName>() + text)
                }
            }

            _searchBoxTextPlaceholder.emit(
                searchBoxTextPlaceholder.value + text
            )
        }
    }

    fun trackRouteBottomSheetEventHandler(event: TrackRouteBottomSheetEvent) {
        when (event) {
            TrackRouteBottomSheetEvent.OnCloseBottomSheet -> sendEffect(TrackRouteBottomSheetEffect.CloseBottomSheet)
            TrackRouteBottomSheetEvent.OnFilterClick -> _trackRouteMainContentState.update {
                it.copy(
                    chooseFilter = true
                )
            }

            is TrackRouteBottomSheetEvent.OnRemoveItem -> onRemoveItem(event.id)
            TrackRouteBottomSheetEvent.OnSortClick -> _trackRouteMainContentState.update {
                it.copy(
                    chooseSorter = true
                )
            }

            TrackRouteBottomSheetEvent.OnStartForegroundService -> sendEffect(
                TrackRouteBottomSheetEffect.StartForegroundService
            )

            TrackRouteBottomSheetEvent.OnStopForegroundService -> sendEffect(
                TrackRouteBottomSheetEffect.StopForegroundService
            )

            TrackRouteBottomSheetEvent.OnToggleEditMode -> _trackRouteMainContentState.update {
                it.copy(
                    isEditMode = !it.isEditMode
                )
            }


            TrackRouteBottomSheetEvent.OnToggleRenderAllTracksSwitch -> _trackRouteMainContentState.update {
                it.copy(
                    isRenderAllTracksSwitchChecked = !it.isRenderAllTracksSwitchChecked
                )
            }

            is TrackRouteBottomSheetEvent.OnItemClick -> {
                viewModelScope.launch {
                    trackRouteRepository.get().getRecordById(event.id).firstOrNull()?.let {
                        _routeRecord.value = it.asRouteRecordUiModel()
                    }
                }
            }

            TrackRouteBottomSheetEvent.OnNavigateBack -> clearRouteRecord()
        }
    }

    private fun sendEffect(effect: TrackRouteBottomSheetEffect) {
        viewModelScope.launch {
            _trackRouteEffect.send(effect)
        }
    }

    private fun onRemoveItem(id: Long) {
        viewModelScope.launch {
            trackRouteRepository.get().removeRecord(id)
        }
    }

    private fun clearRouteRecord() {
        _routeRecord.value = null
    }

    fun setIsRouteServiceRunning(boolean: Boolean) {
        _trackRouteMainContentState.update {
            it.copy(serviceRunning = boolean)
        }
    }

    fun removeTextFromSearchBoxTextPlaceholder(text: SearchBoxTextType) {
        if (searchBoxTextPlaceholder.value.isEmpty()) return

        viewModelScope.launch {
            when (text) {
                is SearchBoxTextType.PoiName -> {
                    _searchBoxTextPlaceholder.emit(searchBoxTextPlaceholder.value.filterIsInstance<SearchBoxTextType.Coordinates>())
                }

                is SearchBoxTextType.Coordinates -> {
                    _searchBoxTextPlaceholder.emit(searchBoxTextPlaceholder.value.filterIsInstance<SearchBoxTextType.PoiName>())
                }
            }
        }
    }

    fun saveAnchorageToHistory(anchorage: AnchorageHistoryUiModel) {
        viewModelScope.launch {
            anchorageHistoryRepository.get().saveAnchorageHistory(anchorage.asAnchorageHistory())
        }
    }

    fun updateAnchorageHistoryTimestamp(id: String, timestamp: Long) {
        viewModelScope.launch {
            anchorageHistoryRepository.get().updateAnchorageHistoryTimestamp(
                id = id,
                timestamp = timestamp
            )
        }
    }

    suspend fun getAnchorageHistoryById(id: String): AnchorageHistoryUiModel? =
        anchorageHistoryRepository.get().getAnchorageHistoryById(id).firstOrNull()
            ?.asAnchorageHistoryUiModel()

    fun setIsAnchorageRepositionEnabled(b: Boolean) {
        _isAnchorageRepositionEnabled.value = b
    }

    fun setExpandedAnchorageRepositionType(type: AnchorageRepositionType?) {
        _expandedAnchorageRepositionType.value = type
    }

    fun setAnchorageCenterPoint(latLng: LatLng?) {
        _anchorageCenterPoint.value = latLng
    }

    fun setSelectedFeatureId(featureType: com.bytecause.map.ui.FeatureType) {
        _selectedFeatureIdFlow.update { featureType }
    }

    fun updateVesselBbox(bbox: LatLngBounds) {
        viewModelScope.launch {
            vesselsBbox.send(bbox)
        }
    }

    fun updateHarbourBbox(bbox: LatLngBounds) {
        viewModelScope.launch {
            harboursBbox.send(bbox)
        }
    }

    fun updatePoiBbox(bounds: LatLngBounds?) {
        poisBbox.update { bounds }
    }

    fun updateAnchoragesBbox(bounds: LatLngBounds) {
        viewModelScope.launch {
            anchoragesBbox.send(bounds)
        }
    }

    fun setIsMeasuring(boolean: Boolean) {
        isMeasuring = boolean
    }

    fun addMeasurePoint(latLng: LatLng) {
        viewModelScope.launch {
            _measurePointsSharedFlow.emit(
                _measurePointsSharedFlow.replayCache.lastOrNull()
                    ?.plus(latLng) ?: listOf(latLng)
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

    fun calculateDistance(latLngList: List<LatLng>): MeasureUnit {
        var distance = 0.0
        for (index in latLngList.indices) {
            if (index == latLngList.lastIndex) break
            distance += latLngList[index].distanceTo(latLngList[index + 1])
        }
        return if (distance > 1000) MeasureUnit.KiloMeters(((distance.toFirstDecimal { this / MetersUnitConvertConstants.KiloMeters.value })))
        else MeasureUnit.Meters(round(distance).toInt())
    }

    fun searchVesselById(id: Int): Flow<VesselInfoModel> =
        vesselsDatabaseRepository.get().searchVesselById(id)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchInPoiCache(placeIds: List<Long>): Flow<List<PoiUiModel>> =
        poiCacheRepository.get().searchInCache(placeIds)
            .flatMapLatest { originalList ->
                if (originalList.size == placeIds.size) {
                    // If the size matches, emit content from poi cache table
                    flowOf(mapList(originalList) { it.asPoiUiModel() })
                } else {
                    // If size does not match, fallback to the radius cache table
                    radiusPoiCacheRepository.get().searchInCache(placeIds)
                        .map { radiusPoiCacheList ->
                            mapList(radiusPoiCacheList) { it.asPoiUiModel() }
                        }
                }
            }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchPoiWithInfoById(id: Long): Flow<PoiUiModelWithTags?> =
        poiCacheRepository.get().searchPoiWithInfoById(id)
            .flatMapLatest { poi ->
                if (poi != null) {
                    // if poi with given id has been found in poi cache table return it
                    flowOf(poi.asPoiUiModelWithTags())
                } else {
                    // if poi hasn't been found, fallback to the radius cache table
                    radiusPoiCacheRepository.get().searchPoiWithInfoById(id)
                        .map { it?.asPoiUiModelWithTags() }
                }
            }

    val loadAllCustomPoi: Flow<List<CustomPoiEntity>> = customPoiRepository.get().loadAllCustomPoi()

    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity> =
        customPoiRepository.get().searchCustomPoiById(id)

    val selectedPoiCategories: StateFlow<Set<String>?> =
        userPreferencesRepository.getSelectedPoiCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val loadPoiCacheByBoundingBox: Flow<List<PoiUiModel>?> = combine(
        poisBbox,
        selectedPoiCategories
    ) { bbox, selectedCategories ->

        bbox.takeIf { it != null }?.let {
            poiCacheRepository.get().loadPoiCacheByBoundingBox(
                minLat = it.latitudeSouth,
                maxLat = it.latitudeNorth,
                minLon = it.longitudeWest,
                maxLon = it.longitudeEast,
                selectedCategories ?: emptySet()
            ).firstOrNull()?.map { poi -> poi.asPoiUiModel() }
        }
    }

    fun searchHarbourById(id: Int): Flow<HarboursModel> =
        harboursDatabaseRepository.get().searchHarbourById(id)

    fun setLocationButtonState(v: Int) {
        viewModelScope.launch {
            _locationButtonStateFlow.emit(v)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> filterVisible(
        latLngBounds: LatLngBounds,
        list: List<T>
    ): List<T> {
        return when (T::class) {
            VesselModel::class -> list.filterIsInstance<VesselModel>().filter { vessel ->
                val latLng = LatLng(
                    latitude = vessel.latitude,
                    longitude = vessel.longitude
                )
                MapUtil.isPositionInBoundingBox(latLng, latLngBounds)
            } as List<T>

            HarboursUiModel::class -> list.filterIsInstance<HarboursUiModel>()
                .filter { harbour ->
                    val latLng = LatLng(
                        latitude = harbour.latitude,
                        longitude = harbour.longitude
                    )
                    MapUtil.isPositionInBoundingBox(latLng, latLngBounds)
                } as List<T>

            else -> listOf()
        }
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
                            it.values.map { customTileProviderList ->
                                customTileProviderList.find { customTileProvider ->
                                    when (val type = customTileProvider.type) {
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
                                        name = name,
                                        url = url,
                                        tileSize = tileSize,
                                        minZoom = minZoom.toFloat(),
                                        maxZoom = maxZoom.toFloat(),
                                    )
                                } ?: run {
                                    (type as? CustomTileProviderType.Raster.Offline)?.run {
                                        TileSources.Raster.Custom.Offline(
                                            name = name,
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
            userPreferencesRepository.saveUserPosition(position.asLatLngModel())
        }
    }

    fun getUserLocation(): Flow<LatLng?> = userPreferencesRepository.getUserPosition()
        .map { it?.asLatLng() }

    fun getCachedTileSourceType(): Flow<TileSources?> = flow {
        userPreferencesRepository.getCachedTileSource().firstOrNull()?.let {
            emit(getCachedTileSourceType(it).firstOrNull())
        }
    }
}
