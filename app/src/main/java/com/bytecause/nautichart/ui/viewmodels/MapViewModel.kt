package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.data.local.room.tables.HarboursEntity
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import com.bytecause.nautichart.data.repository.CustomPoiDatabaseRepository
import com.bytecause.nautichart.data.repository.HarboursDatabaseRepository
import com.bytecause.nautichart.data.repository.HarboursRepository
import com.bytecause.nautichart.data.repository.PoiCacheRepository
import com.bytecause.nautichart.data.repository.UserPreferencesRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.domain.model.VesselMappedEntity
import com.bytecause.nautichart.domain.usecase.VesselsUseCase
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.ui.view.overlay.CustomMarker
import com.bytecause.nautichart.util.MapUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.net.ConnectException
import javax.inject.Inject

// TODO("Refactor")
@HiltViewModel
class MapViewModel @Inject constructor(
    // I didn't use UseCase for this, because I will probably replace this API.
    private val harboursRepository: HarboursRepository,
    private val harboursDatabaseRepository: HarboursDatabaseRepository,
    //
    private val poiCacheRepository: PoiCacheRepository,
    private val customPoiRepository: CustomPoiDatabaseRepository,
    private val vesselsUseCase: VesselsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _harboursFetchingState = MutableStateFlow<UiState<HarboursEntity>?>(null)
    val harboursFetchingState = _harboursFetchingState.asStateFlow()

    private val _vesselsFetchingState = MutableStateFlow<UiState<VesselInfoEntity>?>(null)
    val vesselsFetchingState get() = _vesselsFetchingState.asStateFlow()

    private val _vesselsInAreaSharedFlow = MutableSharedFlow<List<VesselMappedEntity>>()
    val vesselsInAreaSharedFlow get() = _vesselsInAreaSharedFlow.asSharedFlow()

    private var _locationButtonStateFlow = MutableStateFlow<Int?>(null)
    val locationButtonStateFlow get() = _locationButtonStateFlow.asStateFlow()

    private var _harboursVisible = MutableLiveData(false)
    val harboursVisible: LiveData<Boolean> get() = _harboursVisible

    private val _vesselLocationsVisible = MutableLiveData(false)
    val vesselLocationsVisible: LiveData<Boolean> get() = _vesselLocationsVisible

    private var _vesselMarkers: List<CustomMarker>? = null
    val vesselMarkers get() = _vesselMarkers

    private val _isCustomizeDialogVisible = MutableSharedFlow<Boolean>(1)
    val isCustomizeDialogVisible get() = _isCustomizeDialogVisible.asSharedFlow()

    var selectedMarker: CustomMarker? = null
        private set

    fun filterPoiByBoundingBox(boundingBox: BoundingBox): Flow<List<PoiCacheEntity>> =
        poiCacheRepository.loadCachedResults
            .map { list ->
                list.filter {
                    MapUtil.isPositionInBoundingBox(
                        GeoPoint(
                            it.latitude,
                            it.longitude
                        ), boundingBox
                    )
                }
            }

    fun searchVesselById(id: Int): Flow<VesselInfoEntity> = vesselsUseCase.searchVesselById(id)

    fun searchInCache(placeIds: List<Long>): Flow<List<PoiCacheEntity>> = poiCacheRepository.searchInCache(placeIds)

    val loadAllCustomPoi: Flow<List<CustomPoiEntity>> = customPoiRepository.loadAllCustomPoi

    fun searchCustomPoiById(id: Int): Flow<CustomPoiEntity> =
        customPoiRepository.searchCustomPoiById(id)

    private val _harboursInAreaSharedFlow = MutableSharedFlow<List<HarboursEntity>?>()
    val harboursInAreaSharedFlow get() = _harboursInAreaSharedFlow.asSharedFlow()

    val loadAllHarbours: Flow<List<HarboursEntity>> = harboursDatabaseRepository.loadAllHarbours
    val isHarboursDatabaseEmpty: Flow<Boolean> = harboursDatabaseRepository.isHarboursDatabaseEmpty
    fun isHarbourIdInDatabase(idList: List<Int>) = harboursDatabaseRepository.isHarbourIdInDatabase(idList)
    fun searchHarbourById(id: Int): Flow<HarboursEntity> = harboursDatabaseRepository.searchHarbourById(id)
    fun addHarbours(entity: List<HarboursEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            harboursDatabaseRepository.insertAllHarbours(entity)
        }
    }

    fun emitVisibleHarbours(boundingBox: BoundingBox) {
        viewModelScope.launch(Dispatchers.IO) {
            loadAllHarbours.firstOrNull()?.let {
                it.filter { element ->
                    val geoPoint =
                        GeoPoint(element.latitude, element.longitude)
                    MapUtil.isPositionInBoundingBox(geoPoint, boundingBox)
                }.let { entity ->
                    _harboursInAreaSharedFlow.emit(entity)
                }
            }
        }
    }

    fun setIsCustomizeDialogVisible(b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCustomizeDialogVisible.emit(b)
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

    fun toggleVesselLocations() {
        _vesselLocationsVisible.value = _vesselLocationsVisible.value != true
    }

    fun saveVesselsMarkers(markers: List<CustomMarker>?) {
        this._vesselMarkers = markers
    }

    fun setSelectedMarker(marker: CustomMarker?) {
        this.selectedMarker = marker
    }

    fun emitVisibleVessels(
        boundingBox: BoundingBox,
        vesselEntity: List<VesselInfoEntity>? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = vesselEntity ?: vesselsUseCase.fetchVessels().firstOrNull()?.data
            data?.let {
                it.filter { element ->
                    val geoPoint =
                        GeoPoint(element.latitude.toDouble(), element.longitude.toDouble())
                    MapUtil.isPositionInBoundingBox(geoPoint, boundingBox)
                }.map { filteredElements ->
                    mapToVesselMappedEntity(filteredElements)
                }.let { entity ->
                    _vesselsInAreaSharedFlow.emit(entity)
                }
            }
        }
    }

    private fun mapToVesselMappedEntity(vesselInfoEntity: VesselInfoEntity): VesselMappedEntity {
        vesselInfoEntity.apply {
            return VesselMappedEntity(
                id = id.toString(),
                latitude = latitude,
                longitude = longitude,
                type = type,
                heading = heading
            )
        }
    }

    suspend fun fetchVesselsLocation() {
        _vesselsFetchingState.value = UiState(isLoading = true)
        when (val data = vesselsUseCase.fetchVessels().firstOrNull()) {
            is ApiResult.Success -> {
                _vesselsFetchingState.emit(
                    UiState(
                        isLoading = false,
                        items = data.data ?: emptyList()
                    )
                )
            }

            is ApiResult.Failure -> {
                when (data.exception) {
                    is ConnectException -> {
                        _vesselsFetchingState.emit(
                            UiState(
                                isLoading = false,
                                error = UiState.Error.ServiceUnavailable
                            )
                        )
                    }

                    else -> {
                        _vesselsFetchingState.emit(
                            UiState(
                                isLoading = false,
                                error = UiState.Error.NetworkError
                            )
                        )
                    }
                }
            }

            else -> {
                return
            }
        }
        _vesselsFetchingState.value = _vesselsFetchingState.value?.copy(isLoading = false)
    }

    suspend fun fetchHarbours(
        boundingBox: BoundingBox? = null,
        zoomLevel: Double? = null
    ) {
        _harboursFetchingState.value = UiState(isLoading = true)
        when (val data = harboursRepository.getHarbours(boundingBox, zoomLevel)) {
            is ApiResult.Success -> {
                _harboursFetchingState.emit(
                    UiState(
                        isLoading = false,
                        items = data.data ?: emptyList()
                    )
                )
            }

            is ApiResult.Failure -> {
                when (data.exception) {
                    is ConnectException -> {
                        _harboursFetchingState.emit(
                            UiState(
                                isLoading = false,
                                error = UiState.Error.ServiceUnavailable
                            )
                        )
                    }

                    else -> {
                        _harboursFetchingState.emit(
                            UiState(
                                isLoading = false,
                                error = UiState.Error.NetworkError
                            )
                        )
                    }
                }
            }
        }
        _harboursFetchingState.value = _harboursFetchingState.value?.copy(isLoading = false)
    }

    fun getCachedTileSource(name: String?): OnlineTileSourceBase? {
        name ?: return null

        return when (name) {
            TileSourceFactory.MAPNIK.name() -> TileSourceFactory.MAPNIK
            CustomTileSourceFactory.SAT.name() -> CustomTileSourceFactory.SAT
            TileSourceFactory.OpenTopo.name() -> TileSourceFactory.OpenTopo
            else -> null
        }
    }

    // User Preferences DataStore operations.
    fun getFirstRunFlag() = userPreferencesRepository.getFirstRunFlag()


    fun saveUserLocation(position: GeoPoint) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserPosition(position)
        }
    }

    fun getUserLocation() = userPreferencesRepository.getUserPosition()

    fun getCachedTileSource(): Flow<String?> = userPreferencesRepository.getCachedTileSource()
}