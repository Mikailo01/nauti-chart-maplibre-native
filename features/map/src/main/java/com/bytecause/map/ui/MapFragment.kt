package com.bytecause.map.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Range
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.bytecause.data.services.AnchorageAlarmService
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.domain.util.PoiTagsUtil.excludeDescriptionKeysFromTags
import com.bytecause.domain.util.PoiTagsUtil.extractContactsFromTags
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.domain.util.PoiTagsUtil.getPoiType
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.FragmentMapBinding
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.MarkerInfoModel
import com.bytecause.map.ui.model.MeasureUnit
import com.bytecause.map.ui.model.SearchBoxTextType
import com.bytecause.map.ui.viewmodel.AnchorageRepositionType
import com.bytecause.map.ui.viewmodel.MapViewModel
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGES_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGES_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_BORDER_RADIUS_LAYER
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_CENTER_SYMBOL_ICON
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_ICON
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_MOVEMENT_LINE_LAYER
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_RADIUS_MOVE_BY
import com.bytecause.map.util.MapFragmentConstants.ANCHORAGE_SYMBOL_DEFAULT_SIZE
import com.bytecause.map.util.MapFragmentConstants.ANCHOR_CHAIN_LINE_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.ANCHOR_CHAIN_LINE_LAYER
import com.bytecause.map.util.MapFragmentConstants.ANIMATED_CIRCLE_COLOR
import com.bytecause.map.util.MapFragmentConstants.ANIMATED_CIRCLE_RADIUS
import com.bytecause.map.util.MapFragmentConstants.ANIMATE_TO_RADIUS_BOUNDS_PADDING
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_DEFAULT_SIZE
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_ICON_DRAWABLE_KEY_PREFIX
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_PROPERTY_ID_KEY
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY
import com.bytecause.map.util.MapFragmentConstants.CUSTOM_POI_SYMBOL_SELECTED_SIZE
import com.bytecause.map.util.MapFragmentConstants.DEFAULT_BUTTON_STATE
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_ICON
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_DEFAULT_SIZE
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_PROPERTY_ID_KEY
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_SELECTED_SIZE
import com.bytecause.map.util.MapFragmentConstants.LINE_WIDTH
import com.bytecause.map.util.MapFragmentConstants.MAP_MARKER
import com.bytecause.map.util.MapFragmentConstants.MEASURE_LINE_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.MEASURE_LINE_LAYER
import com.bytecause.map.util.MapFragmentConstants.PIN_ICON
import com.bytecause.map.util.MapFragmentConstants.POIS_VISIBILITY_ZOOM_LEVEL
import com.bytecause.map.util.MapFragmentConstants.POI_CATEGORY_KEY
import com.bytecause.map.util.MapFragmentConstants.POI_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.POI_SYMBOL_ICON_SIZE
import com.bytecause.map.util.MapFragmentConstants.POI_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.POI_SYMBOL_NAME_KEY
import com.bytecause.map.util.MapFragmentConstants.POI_SYMBOL_PROPERTY_ID_KEY
import com.bytecause.map.util.MapFragmentConstants.POI_SYMBOL_TEXT_OFFSET_KEY
import com.bytecause.map.util.MapFragmentConstants.PULSING_CIRCLE_ANIMATION_DURATION
import com.bytecause.map.util.MapFragmentConstants.PULSING_CIRCLE_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.PULSING_CIRCLE_LAYER
import com.bytecause.map.util.MapFragmentConstants.SYMBOL_ICON_ANCHOR_BOTTOM
import com.bytecause.map.util.MapFragmentConstants.SYMBOL_ICON_ANCHOR_CENTER
import com.bytecause.map.util.MapFragmentConstants.SYMBOL_ICON_SIZE
import com.bytecause.map.util.MapFragmentConstants.SYMBOL_TYPE
import com.bytecause.map.util.MapFragmentConstants.TRACKING_BUTTON_STATE
import com.bytecause.map.util.MapFragmentConstants.VESSEL_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_DEFAULT_SIZE
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_ICON_DRAWABLE_KEY_PREFIX
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_ICON_ROTATION_KEY
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_PROPERTY_ID_KEY
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_PROPERTY_SELECTED_KEY
import com.bytecause.map.util.MapFragmentConstants.VESSEL_SYMBOL_SELECTED_SIZE
import com.bytecause.map.util.MapFragmentConstants.ZOOM_IN_DEFAULT_LEVEL
import com.bytecause.map.util.MapUtil
import com.bytecause.map.util.MapUtil.calculateBoundsForRadius
import com.bytecause.map.util.MapUtil.calculateZoomForBounds
import com.bytecause.map.util.MapUtil.drawLine
import com.bytecause.map.util.MapUtil.radiusInMetersToRadiusInPixels
import com.bytecause.map.util.navigateToCustomPoiNavigation
import com.bytecause.map.util.navigateToFirstRunNavigation
import com.bytecause.map.util.navigateToSearchNavigation
import com.bytecause.presentation.components.views.CustomTextInputEditText
import com.bytecause.presentation.interfaces.DrawerController
import com.bytecause.presentation.model.PlaceType
import com.bytecause.presentation.model.PointType
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.KeyboardUtils
import com.bytecause.util.color.toHexString
import com.bytecause.util.common.LastClick
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.map.MapUtil.Companion.areCoordinatesValid
import com.bytecause.util.map.MapUtil.Companion.bearingTo
import com.bytecause.util.map.MapUtil.Companion.newLatLngFromDistance
import com.bytecause.util.map.TileSourceLoader
import com.bytecause.util.map.TileSourceLoader.MAIN_LAYER_ID
import com.bytecause.util.poi.PoiUtil
import com.bytecause.util.poi.PoiUtil.createLayerDrawable
import com.bytecause.util.poi.PoiUtil.extractPropImagesFromTags
import com.bytecause.util.poi.PoiUtil.poiSymbolDrawableMap
import com.bytecause.util.string.StringUtil.replaceHttpWithHttps
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.gson.JsonArray
import com.mapbox.android.gestures.MoveGestureDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMap.OnCameraMoveListener
import org.maplibre.android.maps.MapLibreMap.OnMoveListener
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.expressions.Expression.eq
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.switchCase
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconRotate
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineDasharray
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.abs
import kotlin.math.round
import kotlin.properties.Delegates


sealed interface FeatureType {
    data class Pois(val id: Long?) : FeatureType
    data class CustomPoi(val id: Long?) : FeatureType
    data class Vessel(val id: Long?) : FeatureType
    data class Harbour(val id: Long?) : FeatureType
}

private enum class FeatureTypeEnum {
    POIS,
    CUSTOM_POI,
    VESSEL,
    HARBOUR
}

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
    private val binding by viewBinding(FragmentMapBinding::bind)

    private val viewModel: MapViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private var mapView: MapView? = null
    private lateinit var mapLibre: MapLibreMap
    private var locationComponent: LocationComponent? = null

    private lateinit var mapStyle: Style

    private var symbolManager: SymbolManager? = null
    private var boundaryManager: LineManager? = null
    private var markerSymbol: Symbol? = null
    private var vesselsFeatureCollection: FeatureCollection? = null
    private var harboursFeatureCollection: FeatureCollection? = null
    private var customPoiFeatureCollection: FeatureCollection? = null
    private var poisFeatureCollection: FeatureCollection? = null

    private var circleLayerAnimator: Animator? = null

    private val onCameraMoveListener: OnCameraMoveListener = OnCameraMoveListener {
        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let {
            updateAnchorageRadius(mapStyle, latitude = it.latitude)
        }
    }

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val magnetometerReading = FloatArray(3)
    private val accelerometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var sensorManager: SensorManager? = null
    private val sensorListener =
        object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                p0 ?: return

                if (p0.sensor.type == TYPE_ACCELEROMETER) {
                    System.arraycopy(
                        p0.values,
                        0,
                        accelerometerReading,
                        0,
                        accelerometerReading.size
                    )
                } else if (p0.sensor.type == TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(p0.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }

                // Check if we can calculate the rotation matrix
                if (SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerReading,
                        magnetometerReading
                    )
                ) {
                    // Get orientation from the rotation matrix
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    // orientationAngles[0] is the azimuth (bearing) in radians, so convert to degrees
                    val azimuthInRadians = orientationAngles[0]
                    val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                    // Normalize azimuth to [0, 360] range
                    val bearing = Math.round(
                        if (azimuthInDegrees < 0) azimuthInDegrees + 360 else azimuthInDegrees
                    )

                    // update view directly
                    binding.anchorageAlarmBottomSheet
                        .anchorageAlarmBottomSheetRepositionLayout
                        .bearingEditText
                        .setText(bearing.toString())
                }
            }

            override fun onAccuracyChanged(
                p0: Sensor?,
                p1: Int
            ) {
            }
        }

    private val maxBottomSheetHeight = Resources.getSystem().displayMetrics.heightPixels / 2

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetCallback: BottomSheetCallback

    private lateinit var markerBottomSheetLayout: LinearLayout
    private lateinit var markerBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var measureBottomSheetLayout: LinearLayout
    private lateinit var measureBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var anchorageAlarmBottomSheetLayout: LinearLayout
    private lateinit var anchorageAlarmBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private var locationButtonState: Int = DEFAULT_BUTTON_STATE

    private val lastClick = LastClick()

    private var shouldInterceptBackEvent: Boolean by Delegates.observable(false) { _, _, newValue ->
        onBackPressedCallback.isEnabled = newValue
    }

    private var isLocationPermissionGranted: Boolean by Delegates.observable(false) { _, _, granted ->
        if (granted) {
            if (::mapStyle.isInitialized) activateLocationComponent(mapStyle)
        }
    }

    private val locationEngineCallback: LocationEngineCallback<LocationEngineResult> =
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(p0: LocationEngineResult?) {
                if (p0 == null) return

                p0.lastLocation?.run {
                    LatLng(
                        latitude = latitude,
                        longitude = longitude,
                    ).let {
                        if (MapUtil.arePointsWithinDelta(
                                it,
                                mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull(),
                            )
                        ) {
                            return@let
                        }

                        // save location into preferences datastore
                        viewModel.saveUserLocation(it)
                        mapSharedViewModel.setLastKnownPosition(it)
                        locationComponent?.forceLocationUpdate(this)
                    }
                }
            }

            override fun onFailure(p0: java.lang.Exception) {}
        }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                isLocationPermissionGranted = true
            } else {
                findNavController().navigate(R.id.action_mapFragment_to_locationDialogFragment)
            }
        }

    private val onBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    measureBottomSheetBehavior.state == STATE_EXPANDED -> {
                        leaveMeasureDistanceMode()
                    }

                    bottomSheetBehavior.state == STATE_EXPANDED || bottomSheetBehavior.state == STATE_COLLAPSED -> {
                        closeBottomSheetLayout()
                    }

                    markerBottomSheetBehavior.state == STATE_EXPANDED || markerBottomSheetBehavior.state == STATE_COLLAPSED -> {
                        closeMarkerBottomSheetLayout()
                    }

                    anchorageAlarmBottomSheetBehavior.state == STATE_EXPANDED || anchorageAlarmBottomSheetBehavior.state == STATE_COLLAPSED -> {
                        mapSharedViewModel.setShowAnchorageAlarmBottomSheet(false)
                    }

                    mapSharedViewModel.showPoiStateFlow.value != null -> {
                        mapSharedViewModel.setPoiToShow(null)
                    }

                    else -> {
                        if (isEnabled) {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.press_back_again),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        isEnabled = false
                    }
                }
            }
        }

    // Binding isn't available in onCreateView yet, so init only elements that don't require view
    // at the time of their initialization.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Init MapLibre
        MapLibre.getInstance(this.requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // this checks if fragment went through configuration change or not, this will prevent from
        // showing another dialog after device's configuration changes.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (viewModel.getFirstRunFlag().firstOrNull() == null) {
                    findNavController().navigateToFirstRunNavigation()
                }
            }
        }

        windowInsetsController =
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView,
            )

        bottomSheetCallback =
            object : BottomSheetCallback() {
                override fun onStateChanged(
                    bottomSheet: View,
                    newState: Int,
                ) {
                    // Handle state changes
                    when (newState) {
                        STATE_HIDDEN -> {
                            when (bottomSheet.id) {
                                bottomSheetLayout.id -> {
                                    bottomSheetLayout.visibility = View.GONE

                                    if (mapSharedViewModel.geoIntentFlow.replayCache.lastOrNull() != null) {
                                        mapSharedViewModel.resetGeoIntentFlow()
                                    }

                                    removeMarker()
                                }

                                markerBottomSheetLayout.id -> {
                                    markerBottomSheetLayout.visibility = View.GONE

                                    // Resets clicked feature state if necessary
                                    when (viewModel.selectedFeatureIdFlow.value) {
                                        is FeatureType.Vessel -> {
                                            viewModel.setSelectedFeatureId(
                                                (viewModel.selectedFeatureIdFlow.value as FeatureType.Vessel).copy(
                                                    id = null
                                                )
                                            )
                                        }

                                        is FeatureType.CustomPoi -> {
                                            viewModel.setSelectedFeatureId(
                                                (viewModel.selectedFeatureIdFlow.value as FeatureType.CustomPoi).copy(
                                                    id = null
                                                )
                                            )
                                        }

                                        is FeatureType.Harbour -> {
                                            viewModel.setSelectedFeatureId(
                                                (viewModel.selectedFeatureIdFlow.value as FeatureType.Harbour).copy(
                                                    id = null
                                                )
                                            )
                                        }

                                        is FeatureType.Pois -> {
                                            viewModel.setSelectedFeatureId(
                                                (viewModel.selectedFeatureIdFlow.value as FeatureType.Pois).copy(
                                                    id = null
                                                )
                                            )
                                        }

                                        null -> {
                                            if (mapSharedViewModel.latLngFlow.value is PointType.Poi) {
                                                mapSharedViewModel.setLatLng(null)
                                            }
                                            mapSharedViewModel.setSearchPlace(null)
                                            removeSearchBoxText(SearchBoxTextType.PoiName())
                                        }
                                    }
                                }

                                anchorageAlarmBottomSheetLayout.id -> {
                                    anchorageAlarmBottomSheetLayout.visibility = View.GONE
                                    if (AnchorageAlarmService.runningAnchorageAlarm.value.isRunning.not()) {
                                        Log.d("idk", "first remove")
                                        removeAnchorageRadius(mapStyle)
                                        mapSharedViewModel.setAnchorageLocationFromHistoryId(null)
                                    }
                                    viewModel.setIsAnchorageRepositionEnabled(false)
                                }

                                measureBottomSheetLayout.id -> {
                                    measureBottomSheetLayout.visibility = View.GONE
                                }
                            }
                        }

                        else -> return
                    }
                }

                override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float,
                ) {
                }
            }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        isLocationPermissionGranted =
            requireContext().isLocationPermissionGranted().also { granted ->
                if (!granted) setDisabledLocationDrawable()
            }

        bottomSheetLayout = binding.bottomSheetId.bottomSheetLayout
        bottomSheetBehavior =
            BottomSheetBehavior.from(bottomSheetLayout).apply {
                maxHeight = maxBottomSheetHeight
                isHideable = true
                state = STATE_HIDDEN
            }

        markerBottomSheetLayout = binding.markerBottomSheetId.markerBottomSheetLayout
        markerBottomSheetBehavior =
            BottomSheetBehavior.from(markerBottomSheetLayout).apply {
                maxHeight = maxBottomSheetHeight
                isHideable = true
                state = STATE_HIDDEN
            }

        measureBottomSheetLayout = binding.measureDistanceBottomSheet.measureDistanceBottomSheet
        measureBottomSheetBehavior =
            BottomSheetBehavior.from(measureBottomSheetLayout).apply {
                maxHeight = maxBottomSheetHeight
                isHideable = true
                isDraggable = false
                state = STATE_HIDDEN
            }

        anchorageAlarmBottomSheetLayout =
            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetLayout
        anchorageAlarmBottomSheetBehavior =
            BottomSheetBehavior.from(anchorageAlarmBottomSheetLayout).apply {
                maxHeight = maxBottomSheetHeight
                isHideable = true
                isDraggable = false
                state = STATE_HIDDEN
            }

        mapView = binding.mapView

        mapView?.getMapAsync { mapLibreMap ->
            mapLibre = mapLibreMap

            mapLibreMap.apply {
                // Map is set up and the style has been loaded.

                // Get last used tile source from Preferences DataStore on first start if present.
                viewLifecycleOwner.lifecycleScope.launch {
                    (viewModel.getCachedTileSourceType().firstOrNull()
                        ?: DefaultTileSources.MAPNIK).let { tileSource ->

                        loadMapStyle(this@apply) { style ->
                            mapStyle = style

                            /*style.addSource(VectorSource(
                                "vector-source",
                                "mbtiles:///storage/emulated/0/Android/obb/com.bytecause.nautichart/netherlands.mbtiles"
                            ))*/

                            /* val lineLayer = LineLayer(
                                 "netherlands",
                                 "vector-source"
                             )

                             lineLayer.sourceLayer = "main-source"

                             style.addLayer(lineLayer)*/

                            mapSharedViewModel.setTile(tileSource)

                            // Init marker icons
                            style.addImages(
                                hashMapOf(
                                    MAP_MARKER to
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                com.bytecause.core.resources.R.drawable.map_marker,
                                            )?.toBitmap(),
                                    PIN_ICON to
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                com.bytecause.core.resources.R.drawable.pin_icon,
                                            )?.toBitmap(),
                                ),
                            )

                            // Here I defined all collectors that depends on mapLibreMap instance, so I can be sure
                            // that the map is fully loaded.

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.measurePointsSharedFlow.collect { points ->
                                        if (points.isEmpty()) return@collect

                                        // will be true when the user leaves measure mode
                                        if (!viewModel.isMeasuring) {
                                            if (style.getSourceAs<GeoJsonSource>(
                                                    MEASURE_LINE_GEOJSON_SOURCE
                                                ) != null
                                            ) {
                                                style.apply {
                                                    removeLayer(MEASURE_LINE_LAYER)
                                                    removeSource(MEASURE_LINE_GEOJSON_SOURCE)
                                                }

                                                symbolManager?.deleteAll()
                                                mapSharedViewModel.setLatLng(PointType.Marker(points.first()))
                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLng(
                                                        points.first(),
                                                    ),
                                                )

                                                // latLngFlow state restored, now we can clear entire list of
                                                // measure points.
                                                viewModel.clearMeasurePoints(entireClear = true)
                                            }
                                            return@collect
                                        }

                                        if (style.getSourceAs<GeoJsonSource>(
                                                MEASURE_LINE_GEOJSON_SOURCE
                                            ) != null
                                        ) {
                                            style.apply {
                                                removeLayer(MEASURE_LINE_LAYER)
                                                removeSource(MEASURE_LINE_GEOJSON_SOURCE)
                                            }
                                        }

                                        val lineFeature = Feature.fromGeometry(
                                            LineString.fromLngLats(
                                                points.map {
                                                    Point.fromLngLat(it.longitude, it.latitude)
                                                }.let {
                                                    // add center point to the feature list if not null,
                                                    // otherwise return original feature list
                                                    mapLibreMap.cameraPosition.target?.let { centerPoint ->
                                                        it.plus(
                                                            Point.fromLngLat(
                                                                centerPoint.longitude,
                                                                centerPoint.latitude
                                                            )
                                                        )
                                                    } ?: it
                                                }
                                            )
                                        )

                                        val measureLineGeojsonSource =
                                            GeoJsonSource(
                                                MEASURE_LINE_GEOJSON_SOURCE,
                                                lineFeature
                                            )
                                        val measureLineLayer = LineLayer(
                                            MEASURE_LINE_LAYER,
                                            MEASURE_LINE_GEOJSON_SOURCE
                                        )

                                        measureLineLayer.setProperties(
                                            lineWidth(3f),
                                            lineDasharray(arrayOf(1f, 1f)),
                                            lineColor(
                                                requireContext().getColor(com.bytecause.core.resources.R.color.dark_blue)
                                                    .toHexString()
                                            )
                                        )

                                        style.apply {
                                            addSource(measureLineGeojsonSource)
                                            addLayer(measureLineLayer)
                                        }

                                        if (binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility == View.GONE) {
                                            enterMeasureDistanceMode()
                                        }

                                        symbolManager?.deleteAll()

                                        if (symbolManager == null) {
                                            symbolManager =
                                                SymbolManager(mapView!!, mapLibreMap, style)
                                        }

                                        points.map { point ->
                                            symbolManager?.create(
                                                SymbolOptions()
                                                    .withLatLng(point)
                                                    .withIconSize(SYMBOL_ICON_SIZE)
                                                    .withIconImage(PIN_ICON)
                                                    .withIconAnchor(
                                                        SYMBOL_ICON_ANCHOR_BOTTOM
                                                    ),
                                            )
                                        }.let {
                                            symbolManager?.update(it)
                                        }

                                        viewModel.calculateDistance(points)
                                            .let { distance ->

                                                val (value, unit) = when (distance) {
                                                    is MeasureUnit.Meters -> distance.value to "M"
                                                    is MeasureUnit.KiloMeters -> distance.value to "KM"
                                                }

                                                val distanceTextViewText =
                                                    getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                                                        value,
                                                        unit
                                                    )

                                                binding.measureDistanceBottomSheet.distanceTextview.text =
                                                    distanceTextViewText
                                            }
                                    }
                                }
                            }

                            fun addMarker(point: PointType) {
                                if (markerSymbol != null) {
                                    symbolManager?.delete(markerSymbol)
                                }

                                val latLng = (point as? PointType.Marker)?.latLng
                                    ?: (point as? PointType.Poi)?.latLng
                                    ?: (point as PointType.Address).latLng

                                // Add a new symbol at specified lat/lon.
                                markerSymbol =
                                    symbolManager?.create(
                                        SymbolOptions()
                                            .withLatLng(latLng)
                                            .withIconImage(MAP_MARKER)
                                            .withIconSize(SYMBOL_ICON_SIZE)
                                            .withIconAnchor(SYMBOL_ICON_ANCHOR_BOTTOM),
                                    )

                                // Disable symbol collisions and update symbol
                                symbolManager?.apply {
                                    iconAllowOverlap = true
                                    iconIgnorePlacement = true
                                    update(markerSymbol)
                                }

                                when (point) {
                                    is PointType.Poi -> showPoiInfo(poiId = point.id)
                                    is PointType.Marker -> openBottomSheetLayout(point.latLng)
                                    is PointType.Address -> {
                                        // nothing
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.latLngFlow.collect { point ->
                                        point ?: run {
                                            if (markerSymbol == null || symbolManager == null) return@collect

                                            symbolManager?.delete(markerSymbol)
                                            markerSymbol = null

                                            return@collect
                                        }
                                        if (viewModel.isMeasuring) return@collect

                                        if (markerBottomSheetBehavior.state == STATE_EXPANDED) {
                                            closeMarkerBottomSheetLayout()
                                        }

                                        if (symbolManager == null) {
                                            symbolManager =
                                                SymbolManager(
                                                    mapView!!,
                                                    mapLibreMap,
                                                    style
                                                )
                                        }

                                        addMarker(point)
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.searchPlace.collect { place ->
                                        place ?: return@collect

                                        boundaryManager?.deleteAll()

                                        when (place) {
                                            is PlaceType.Poi -> {
                                                val latLng = LatLng(
                                                    latitude = place.latitude,
                                                    longitude = place.longitude
                                                )

                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        latLng,
                                                        13.0
                                                    )
                                                )

                                                mapSharedViewModel.setLatLng(
                                                    PointType.Poi(
                                                        latLng = latLng,
                                                        id = place.id
                                                    )
                                                )

                                                setSearchBoxText(SearchBoxTextType.PoiName(place.name))
                                            }

                                            is PlaceType.Address -> {
                                                val latLng = LatLng(
                                                    place.placeModel.latitude,
                                                    place.placeModel.longitude
                                                )

                                                place.placeModel.polygonCoordinates.takeIf { coordinates -> coordinates.isNotEmpty() }
                                                    ?.let { encodedPolygon ->
                                                        com.bytecause.util.algorithms.PolylineAlgorithms()
                                                            .decode(encodedPolygon)
                                                            .let { polylineList ->

                                                                boundaryManager =
                                                                    LineManager(
                                                                        mapView!!,
                                                                        mapLibreMap,
                                                                        style
                                                                    ).apply {
                                                                        drawLine(
                                                                            polylineList = polylineList,
                                                                            lineManager = this,
                                                                            lineColor = requireContext().getColor(
                                                                                com.bytecause.core.resources.R.color.black
                                                                            ),
                                                                            lineWidth = LINE_WIDTH,
                                                                        )
                                                                    }

                                                                mapLibreMap.animateCamera(
                                                                    CameraUpdateFactory.newLatLngBounds(
                                                                        bounds =
                                                                        LatLngBounds.fromLatLngs(
                                                                            polylineList.map {
                                                                                LatLng(
                                                                                    it.latitude,
                                                                                    it.longitude
                                                                                )
                                                                            }
                                                                        ),
                                                                        padding = 50
                                                                    )
                                                                )
                                                            }
                                                    } ?: run {
                                                    mapLibreMap.animateCamera(
                                                        CameraUpdateFactory.newLatLngZoom(
                                                            latLng,
                                                            13.0
                                                        )
                                                    )
                                                }

                                                mapSharedViewModel.setLatLng(
                                                    PointType.Address(
                                                        latLng
                                                    )
                                                )
                                                setSearchBoxText(SearchBoxTextType.PoiName(place.placeModel.name))
                                            }
                                        }
                                    }
                                }
                            }

                            // Shows poi on map of the given category (e.g.: cinema, cafe, ...)
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.showPoiStateFlow.collect { poiMap ->
                                        style.apply {
                                            if (getSourceAs<GeoJsonSource>(
                                                    POI_GEOJSON_SOURCE
                                                ) != null
                                            ) {
                                                removeLayer(POI_SYMBOL_LAYER)
                                                removeSource(POI_GEOJSON_SOURCE)
                                                removeSearchBoxText(SearchBoxTextType.PoiName())
                                            }
                                        }

                                        if (poiMap.isNullOrEmpty()) return@collect

                                        if (bottomSheetBehavior.state == STATE_EXPANDED) {
                                            closeBottomSheetLayout()
                                        }

                                        // poiMap holds category name and List of IDs which are used for search in database.
                                        viewModel.searchInPoiCache(poiMap.values.flatten())
                                            .firstOrNull()?.let { poiUiModelList ->
                                                if (poiUiModelList.isEmpty()) return@collect

                                                // Cache drawable resources
                                                val drawableCache =
                                                    mutableMapOf<String, Drawable>()

                                                setSearchBoxText(
                                                    SearchBoxTextType.PoiName(poiMap.keys.first())
                                                )

                                                val points = mutableListOf<LatLng>()
                                                val features = mutableListOf<Feature>()

                                                for (poi in poiUiModelList) {
                                                    points.add(
                                                        LatLng(
                                                            latitude = poi.latitude,
                                                            longitude = poi.longitude,
                                                        ),
                                                    )

                                                    drawableCache.getOrPut(poi.category) {
                                                        createLayerDrawable(
                                                            context = requireContext(),
                                                            category = poi.category,
                                                            drawable = poiSymbolDrawableMap[poi.category]?.let {
                                                                ContextCompat.getDrawable(
                                                                    requireContext(),
                                                                    it
                                                                )
                                                            } ?: ContextCompat.getDrawable(
                                                                requireContext(),
                                                                com.bytecause.core.resources.R.drawable.circle,
                                                            )
                                                        )
                                                    }.let {
                                                        features.add(
                                                            Feature.fromGeometry(
                                                                Point.fromLngLat(
                                                                    poi.longitude,
                                                                    poi.latitude,
                                                                ),
                                                            ).apply {
                                                                val splittedNames =
                                                                    poi.name.split(" ")

                                                                val textOffsetArray =
                                                                    JsonArray().apply {
                                                                        add(when {
                                                                            splittedNames.all { name -> name.length <= 4 } -> -2f
                                                                            splittedNames.all { name -> name.length <= 5 } -> -2.5f
                                                                            splittedNames.any { name -> name.length == 9 } -> -3.5f
                                                                            splittedNames.any { name -> name.length >= 10 } -> -4f
                                                                            else -> -3f
                                                                        })
                                                                        add(-1f)
                                                                    }

                                                                addProperty(
                                                                    POI_SYMBOL_TEXT_OFFSET_KEY,
                                                                    textOffsetArray
                                                                )
                                                                addStringProperty(
                                                                    SYMBOL_TYPE,
                                                                    FeatureTypeEnum.POIS.name
                                                                )
                                                                addStringProperty(
                                                                    POI_CATEGORY_KEY,
                                                                    poi.category,
                                                                )
                                                                addStringProperty(
                                                                    POI_SYMBOL_NAME_KEY,
                                                                    poi.name
                                                                )
                                                                addNumberProperty(
                                                                    POI_SYMBOL_PROPERTY_ID_KEY,
                                                                    poi.id
                                                                )

                                                                if ((viewModel.selectedFeatureIdFlow.value as? FeatureType.Pois)?.id == poi.id) {
                                                                    // start pulsing animation
                                                                    updatePulsingCircle(
                                                                        style,
                                                                        geometry() as Point
                                                                    )
                                                                }
                                                            },
                                                        )
                                                    }
                                                }

                                                poisFeatureCollection =
                                                    FeatureCollection.fromFeatures(
                                                        features
                                                    )

                                                val geoJsonSource =
                                                    GeoJsonSource(
                                                        POI_GEOJSON_SOURCE,
                                                        poisFeatureCollection
                                                    )

                                                val symbolLayer =
                                                    SymbolLayer(
                                                        POI_SYMBOL_LAYER,
                                                        POI_GEOJSON_SOURCE
                                                    )
                                                        .withProperties(
                                                            textField(
                                                                get(
                                                                    POI_SYMBOL_NAME_KEY,
                                                                )
                                                            ),
                                                            textMaxWidth(1f),
                                                            textSize(12f),
                                                            // font must be included in json style
                                                            // e.g.: "glyphs": "asset://glyphs/{fontstack}/{range}.pbf"
                                                            textFont(arrayOf("Open Sans Semibold")),
                                                            textOffset(
                                                                get(
                                                                    POI_SYMBOL_TEXT_OFFSET_KEY
                                                                )
                                                            ),
                                                            iconImage(
                                                                get(
                                                                    POI_CATEGORY_KEY,
                                                                ),
                                                            ),
                                                            iconSize(POI_SYMBOL_ICON_SIZE),
                                                            iconAnchor(SYMBOL_ICON_ANCHOR_BOTTOM),
                                                        )

                                                style.apply {
                                                    // iterate over drawable map entries and add it's values into
                                                    // maplibre's style
                                                    drawableCache.entries.forEach { entry ->
                                                        addImage(entry.key, entry.value)
                                                    }
                                                    style.getSourceAs<GeoJsonSource>(
                                                        POI_GEOJSON_SOURCE
                                                    )?.let {
                                                        removeLayer(POI_SYMBOL_LAYER)
                                                        removeSource(it)
                                                    }
                                                    addSource(geoJsonSource)
                                                    addLayer(symbolLayer)
                                                }

                                                val bounds =
                                                    LatLngBounds.fromLatLngs(points)

                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngBounds(
                                                        bounds,
                                                        50
                                                    )
                                                )
                                            }
                                    }
                                }
                            }

                            // Notify ui that show all pois state has changed.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.selectedPoiCategories.collect { selectedCategories ->
                                        if (selectedCategories.isNullOrEmpty()) {
                                            viewModel.updatePoiBbox(null)
                                        } else {
                                            viewModel.updatePoiBbox(mapLibreMap.projection.visibleRegion.latLngBounds)
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.loadPoiCacheByBoundingBox.collect { poiList ->
                                        // If showPoiStateFlow is not null, meaning that the user is
                                        // viewing specific category of pois, return collect.
                                        // I am using same GeoJson source for these pois, so this will
                                        // avoid any potential collisions.
                                        if (mapSharedViewModel.showPoiStateFlow.value != null) return@collect


                                        if (poiList.isNullOrEmpty()) {
                                            style.apply {
                                                getSourceAs<GeoJsonSource>(
                                                    POI_GEOJSON_SOURCE
                                                )?.let {
                                                    removeLayer(POI_SYMBOL_LAYER)
                                                    removeSource(it)
                                                }
                                            }
                                            return@collect
                                        }

                                        val features = mutableListOf<Feature>()
                                        val drawableCache = HashMap<String, Drawable>()

                                        for (poi in poiList) {

                                            // getIdentifier() is inefficient, so I used Map to keep track of
                                            // found drawables, so it won't look up for same drawable again and again.
                                            drawableCache.getOrPut(poi.category) {
                                                createLayerDrawable(
                                                    context = requireContext(),
                                                    category = poi.category,
                                                    drawable = poiSymbolDrawableMap[PoiUtil.unifyPoiDrawables(
                                                        poi.category
                                                    )]?.let {
                                                        ContextCompat.getDrawable(
                                                            requireContext(),
                                                            it
                                                        )
                                                    } ?: ContextCompat.getDrawable(
                                                        requireContext(),
                                                        com.bytecause.core.resources.R.drawable.circle,
                                                    )
                                                )
                                            }.let {
                                                features.add(
                                                    Feature.fromGeometry(
                                                        Point.fromLngLat(
                                                            poi.longitude,
                                                            poi.latitude,
                                                        ),
                                                    ).apply {
                                                        val splittedNames = poi.name.split(" ")

                                                        val textOffsetArray = JsonArray().apply {
                                                            add(when {
                                                                splittedNames.all { name -> name.length <= 4 } -> -2f
                                                                splittedNames.all { name -> name.length <= 5 } -> -2.5f
                                                                splittedNames.any { name -> name.length == 9 } -> -3.5f
                                                                splittedNames.any { name -> name.length >= 10 } -> -4f
                                                                else -> -3f
                                                            })
                                                            add(-1f)
                                                        }

                                                        addProperty(
                                                            POI_SYMBOL_TEXT_OFFSET_KEY,
                                                            textOffsetArray
                                                        )
                                                        addStringProperty(
                                                            SYMBOL_TYPE,
                                                            FeatureTypeEnum.POIS.name
                                                        )
                                                        addStringProperty(
                                                            POI_CATEGORY_KEY,
                                                            poi.category,
                                                        )
                                                        addStringProperty(
                                                            POI_SYMBOL_NAME_KEY,
                                                            poi.name
                                                        )
                                                        addNumberProperty(
                                                            POI_SYMBOL_PROPERTY_ID_KEY,
                                                            poi.id
                                                        )

                                                        if ((viewModel.selectedFeatureIdFlow.value as? FeatureType.Pois)?.id == poi.id) {
                                                            // start pulsing animation
                                                            updatePulsingCircle(
                                                                style,
                                                                geometry() as Point
                                                            )
                                                        }
                                                    },
                                                )
                                            }
                                        }

                                        poisFeatureCollection =
                                            FeatureCollection.fromFeatures(features)

                                        style.getSourceAs<GeoJsonSource>(POI_GEOJSON_SOURCE)
                                            ?.setGeoJson(poisFeatureCollection) ?: run {

                                            val geoJsonSource =
                                                GeoJsonSource(
                                                    POI_GEOJSON_SOURCE,
                                                    poisFeatureCollection
                                                )

                                            val symbolLayer =
                                                SymbolLayer(
                                                    POI_SYMBOL_LAYER,
                                                    POI_GEOJSON_SOURCE
                                                )
                                                    .withProperties(
                                                        textField(
                                                            get(
                                                                POI_SYMBOL_NAME_KEY,
                                                            )
                                                        ),
                                                        textMaxWidth(1f),
                                                        textSize(12f),
                                                        // font must be included in json style
                                                        // e.g.: "glyphs": "asset://glyphs/{fontstack}/{range}.pbf"
                                                        textFont(arrayOf("Open Sans Semibold")),
                                                        textOffset(get(POI_SYMBOL_TEXT_OFFSET_KEY)),
                                                        iconImage(
                                                            get(
                                                                POI_CATEGORY_KEY,
                                                            ),
                                                        ),
                                                        iconSize(POI_SYMBOL_ICON_SIZE),
                                                        iconAnchor(SYMBOL_ICON_ANCHOR_BOTTOM),
                                                    )

                                            style.apply {
                                                addSource(geoJsonSource)
                                                addLayer(symbolLayer)
                                            }
                                        }

                                        style.apply {
                                            // iterate over drawable map entries and add it's values into
                                            // maplibre's style
                                            drawableCache.entries.forEach { entry ->
                                                addImage(entry.key, entry.value)
                                            }
                                        }
                                    }
                                }
                            }

                            // Draw vessels in the given bounding box.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.vesselsFlow.collect { vessels ->
                                        if (!viewModel.isAisActivated.value) return@collect
                                        if (vessels.isEmpty()) return@collect

                                        val drawableCache = mutableMapOf<String, Drawable>()
                                        val features = mutableListOf<Feature>()

                                        for (vessel in vessels) {
                                            if (drawableCache[VESSEL_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + vessel.type] == null) {
                                                ContextCompat.getDrawable(
                                                    requireContext(),
                                                    com.bytecause.core.resources.R.drawable.vessel_marker,
                                                )?.apply {
                                                    ContextCompat.getColor(
                                                        requireContext(),
                                                        MapUtil.determineVesselColorType(vessel.type),
                                                    ).also { color ->
                                                        setTint(color)
                                                    }
                                                }?.let {
                                                    drawableCache[VESSEL_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + vessel.type] =
                                                        it
                                                }
                                            }

                                            features.add(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        vessel.longitude,
                                                        vessel.latitude,
                                                    ),
                                                ).apply {
                                                    addNumberProperty(
                                                        VESSEL_SYMBOL_ICON_ROTATION_KEY,
                                                        vessel.heading.takeIf { it.isNotBlank() }
                                                            ?.toFloat()
                                                            ?: 0f,
                                                    )
                                                    addStringProperty(
                                                        VESSEL_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY,
                                                        VESSEL_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + vessel.type,
                                                    )
                                                    addNumberProperty(
                                                        VESSEL_SYMBOL_PROPERTY_ID_KEY,
                                                        vessel.id.toLong()
                                                    )
                                                    addStringProperty(
                                                        SYMBOL_TYPE,
                                                        FeatureTypeEnum.VESSEL.name
                                                    )
                                                    addBooleanProperty(
                                                        VESSEL_SYMBOL_PROPERTY_SELECTED_KEY,
                                                        // Check if vessel is selected
                                                        ((viewModel.selectedFeatureIdFlow.value as? FeatureType.Vessel)?.id == vessel.id.toLong()).takeIf { it }
                                                            ?.also {
                                                                // start pulsing animation
                                                                updatePulsingCircle(
                                                                    style,
                                                                    geometry() as Point
                                                                )
                                                            },
                                                    )
                                                },
                                            )
                                        }

                                        vesselsFeatureCollection =
                                            FeatureCollection.fromFeatures(features)

                                        style.getSourceAs<GeoJsonSource>(VESSEL_GEOJSON_SOURCE)
                                            ?.setGeoJson(vesselsFeatureCollection) ?: run {

                                            val geoJsonSource =
                                                GeoJsonSource(
                                                    VESSEL_GEOJSON_SOURCE,
                                                    vesselsFeatureCollection,
                                                    GeoJsonOptions()
                                                        .withCluster(true)
                                                        .withClusterMaxZoom(2)
                                                )

                                            val unclusteredLayer =
                                                SymbolLayer(
                                                    VESSEL_SYMBOL_LAYER,
                                                    VESSEL_GEOJSON_SOURCE
                                                ).apply {
                                                    setProperties(
                                                        iconRotate(
                                                            get(
                                                                VESSEL_SYMBOL_ICON_ROTATION_KEY
                                                            )
                                                        ),
                                                        iconImage(
                                                            get(
                                                                VESSEL_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY
                                                            )
                                                        ),
                                                        iconSize(
                                                            switchCase(
                                                                eq(
                                                                    get(VESSEL_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    true
                                                                ),
                                                                literal(VESSEL_SYMBOL_SELECTED_SIZE),
                                                                eq(
                                                                    get(VESSEL_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    false
                                                                ),
                                                                literal(VESSEL_SYMBOL_DEFAULT_SIZE),
                                                                literal(VESSEL_SYMBOL_DEFAULT_SIZE),
                                                            ),
                                                        ),
                                                        iconAnchor(SYMBOL_ICON_ANCHOR_CENTER),
                                                    )
                                                }

                                            style.apply {
                                                addSource(geoJsonSource)
                                                addLayer(unclusteredLayer)
                                            }
                                        }

                                        style.apply {
                                            drawableCache.entries.forEach { entry ->
                                                addImage(entry.key, entry.value)
                                            }
                                        }
                                    }
                                }
                            }

                            // Draw harbours in the given bounding box.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.harboursFlow.collect { harbours ->
                                        if (!viewModel.areHarboursVisible.value) return@collect
                                        if (harbours.isEmpty()) return@collect

                                        val features = mutableListOf<Feature>()

                                        for (harbour in harbours) {
                                            features.add(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        harbour.longitude,
                                                        harbour.latitude,
                                                    ),
                                                ).apply {
                                                    addNumberProperty(
                                                        HARBOUR_SYMBOL_PROPERTY_ID_KEY,
                                                        harbour.id.toLong()
                                                    )
                                                    addStringProperty(
                                                        SYMBOL_TYPE,
                                                        FeatureTypeEnum.HARBOUR.name
                                                    )
                                                    addBooleanProperty(
                                                        HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY,
                                                        // Check if vessel is selected
                                                        ((viewModel.selectedFeatureIdFlow.value as? FeatureType.Harbour)?.id == harbour.id.toLong()).takeIf { it }
                                                            ?.also {
                                                                // start pulsing animation
                                                                updatePulsingCircle(
                                                                    style,
                                                                    geometry() as Point
                                                                )
                                                            },
                                                    )
                                                },
                                            )
                                        }

                                        harboursFeatureCollection =
                                            FeatureCollection.fromFeatures(features)

                                        style.getSourceAs<GeoJsonSource>(HARBOUR_GEOJSON_SOURCE)
                                            ?.setGeoJson(harboursFeatureCollection) ?: run {

                                            val geoJsonSource =
                                                GeoJsonSource(
                                                    HARBOUR_GEOJSON_SOURCE,
                                                    harboursFeatureCollection,
                                                    GeoJsonOptions()
                                                        .withCluster(true)
                                                        .withClusterMaxZoom(2)
                                                )

                                            val unclusteredLayer =
                                                SymbolLayer(
                                                    HARBOUR_SYMBOL_LAYER,
                                                    HARBOUR_GEOJSON_SOURCE
                                                ).apply {
                                                    setProperties(
                                                        iconImage(HARBOUR_ICON),
                                                        iconSize(
                                                            switchCase(
                                                                eq(
                                                                    get(HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    true
                                                                ),
                                                                literal(HARBOUR_SYMBOL_SELECTED_SIZE),
                                                                eq(
                                                                    get(HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    false
                                                                ),
                                                                literal(HARBOUR_SYMBOL_DEFAULT_SIZE),
                                                                literal(HARBOUR_SYMBOL_DEFAULT_SIZE),
                                                            ),
                                                        ),
                                                        iconAnchor(SYMBOL_ICON_ANCHOR_CENTER),
                                                    )
                                                }

                                            style.apply {
                                                addSource(geoJsonSource)
                                                addLayer(unclusteredLayer)
                                            }
                                        }

                                        style.apply {
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                com.bytecause.core.resources.R.drawable.harbour_marker_icon
                                            )?.let {
                                                addImage(
                                                    HARBOUR_ICON,
                                                    it
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // This will render all custom pois created by the user if present.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.loadAllCustomPoi.collect { customPoiList ->
                                        if (customPoiList.isEmpty()) {
                                            style.apply {
                                                if (getSourceAs<GeoJsonSource>(
                                                        CUSTOM_POI_GEOJSON_SOURCE
                                                    ) != null
                                                ) {
                                                    removeLayer(CUSTOM_POI_SYMBOL_LAYER)
                                                    removeSource(CUSTOM_POI_GEOJSON_SOURCE)
                                                }
                                            }
                                            return@collect
                                        }

                                        val features = mutableListOf<Feature>()
                                        val drawableCache = HashMap<String, Drawable>()

                                        for (customPoi in customPoiList) {
                                            if (drawableCache[CUSTOM_POI_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + customPoi.drawableResourceName] == null) {
                                                ContextCompat.getDrawable(
                                                    requireContext(),
                                                    resources.getIdentifier(
                                                        customPoi.drawableResourceName,
                                                        "drawable",
                                                        requireContext().packageName,
                                                    ),
                                                )?.let {
                                                    drawableCache[CUSTOM_POI_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + customPoi.drawableResourceName] =
                                                        it
                                                }
                                            }

                                            features.add(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        customPoi.longitude,
                                                        customPoi.latitude,
                                                    ),
                                                ).apply {
                                                    addStringProperty(
                                                        CUSTOM_POI_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY,
                                                        CUSTOM_POI_SYMBOL_ICON_DRAWABLE_KEY_PREFIX + customPoi.drawableResourceName,
                                                    )
                                                    addNumberProperty(
                                                        CUSTOM_POI_SYMBOL_PROPERTY_ID_KEY,
                                                        customPoi.poiId
                                                    )
                                                    addStringProperty(
                                                        SYMBOL_TYPE,
                                                        FeatureTypeEnum.CUSTOM_POI.name
                                                    )
                                                    addBooleanProperty(
                                                        CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY,
                                                        ((viewModel.selectedFeatureIdFlow.value as? FeatureType.CustomPoi)?.id == customPoi.poiId).takeIf { it }
                                                            ?.also {
                                                                updatePulsingCircle(
                                                                    style,
                                                                    geometry() as Point
                                                                )
                                                            }
                                                    )
                                                },
                                            )
                                        }

                                        customPoiFeatureCollection =
                                            FeatureCollection.fromFeatures(features)

                                        style.getSourceAs<GeoJsonSource>(
                                            CUSTOM_POI_GEOJSON_SOURCE
                                        )
                                            ?.setGeoJson(customPoiFeatureCollection)
                                            ?: run {
                                                val geoJsonSource =
                                                    GeoJsonSource(
                                                        CUSTOM_POI_GEOJSON_SOURCE,
                                                        customPoiFeatureCollection
                                                    )

                                                val symbolLayer =
                                                    SymbolLayer(
                                                        CUSTOM_POI_SYMBOL_LAYER,
                                                        CUSTOM_POI_GEOJSON_SOURCE,
                                                    ).withProperties(
                                                        iconImage(
                                                            get(
                                                                CUSTOM_POI_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY
                                                            )
                                                        ),
                                                        iconSize(
                                                            switchCase(
                                                                eq(
                                                                    get(CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    true,
                                                                ),
                                                                literal(
                                                                    CUSTOM_POI_SYMBOL_SELECTED_SIZE
                                                                ),
                                                                eq(
                                                                    get(CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY),
                                                                    false,
                                                                ),
                                                                literal(
                                                                    CUSTOM_POI_SYMBOL_DEFAULT_SIZE
                                                                ),
                                                                literal(
                                                                    CUSTOM_POI_SYMBOL_DEFAULT_SIZE
                                                                ),
                                                            ),
                                                        ),
                                                        iconAnchor(SYMBOL_ICON_ANCHOR_CENTER),
                                                    )

                                                style.apply {
                                                    addSource(geoJsonSource)
                                                    addLayer(symbolLayer)
                                                }
                                            }

                                        style.apply {
                                            drawableCache.entries.forEach { entry ->
                                                addImage(entry.key, entry.value)
                                            }
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.anchoragesFlow.collect { anchorageList ->
                                        if (!viewModel.areAnchoragesVisible.value) return@collect
                                        if (anchorageList.isEmpty()) return@collect

                                        val features = mutableListOf<Feature>()

                                        for (harbour in anchorageList) {
                                            features.add(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        harbour.longitude,
                                                        harbour.latitude,
                                                    ),
                                                )
                                            )
                                        }

                                        style.getSourceAs<GeoJsonSource>(ANCHORAGES_GEOJSON_SOURCE)
                                            ?.setGeoJson(FeatureCollection.fromFeatures(features))
                                            ?: run {

                                                val geoJsonSource =
                                                    GeoJsonSource(
                                                        ANCHORAGES_GEOJSON_SOURCE,
                                                        FeatureCollection.fromFeatures(features),
                                                        GeoJsonOptions()
                                                            .withCluster(true)
                                                            .withClusterMaxZoom(2)
                                                    )

                                                val unclusteredLayer =
                                                    SymbolLayer(
                                                        ANCHORAGES_SYMBOL_LAYER,
                                                        ANCHORAGES_GEOJSON_SOURCE
                                                    ).apply {
                                                        setProperties(
                                                            iconImage(ANCHORAGE_ICON),
                                                            iconAnchor(SYMBOL_ICON_ANCHOR_CENTER),
                                                            iconSize(ANCHORAGE_SYMBOL_DEFAULT_SIZE)
                                                        )
                                                    }

                                                style.apply {
                                                    addSource(geoJsonSource)
                                                    addLayer(unclusteredLayer)
                                                }
                                            }

                                        style.apply {
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                com.bytecause.core.resources.R.drawable.anchor
                                            )?.let {
                                                addImage(
                                                    ANCHORAGE_ICON,
                                                    it
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Notify ui that vessel locations visibility state has changed.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.isAisActivated.collect { isActivated ->
                                        if (!::mapLibre.isInitialized) return@collect

                                        if (isActivated) {
                                            viewModel.updateVesselBbox(mapLibre.projection.visibleRegion.latLngBounds)
                                        } else {
                                            if (style.getSourceAs<GeoJsonSource>(
                                                    VESSEL_GEOJSON_SOURCE
                                                ) == null
                                            ) return@collect

                                            // Remove vessels layer and source
                                            style.apply {
                                                removeLayer(VESSEL_SYMBOL_LAYER)
                                                removeSource(VESSEL_GEOJSON_SOURCE)
                                                removePulsingCircleLayer(this)

                                                vesselsFeatureCollection = null
                                            }
                                        }
                                    }
                                }
                            }

                            // Notify ui that harbour locations visibility state has changed.
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.areHarboursVisible.collect { isVisible ->

                                        if (isVisible) {
                                            viewModel.updateHarbourBbox(mapLibreMap.projection.visibleRegion.latLngBounds)
                                        } else {
                                            if (style.getSourceAs<GeoJsonSource>(
                                                    HARBOUR_GEOJSON_SOURCE
                                                ) == null
                                            ) return@collect

                                            // Remove harbours layer and source
                                            style.apply {
                                                removeLayer(HARBOUR_SYMBOL_LAYER)
                                                removeSource(HARBOUR_GEOJSON_SOURCE)
                                                removePulsingCircleLayer(this)

                                                harboursFeatureCollection = null
                                            }
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.areAnchoragesVisible.collect { isVisible ->
                                        if (isVisible) {
                                            viewModel.updateAnchoragesBbox(mapLibreMap.projection.visibleRegion.latLngBounds)
                                        } else {
                                            if (style.getSourceAs<GeoJsonSource>(
                                                    ANCHORAGES_GEOJSON_SOURCE
                                                ) == null
                                            ) return@collect

                                            // Remove anchorages layer and source
                                            style.apply {
                                                removeLayer(ANCHORAGES_SYMBOL_LAYER)
                                                removeSource(ANCHORAGES_GEOJSON_SOURCE)
                                                removePulsingCircleLayer(this)

                                                //harboursFeatureCollection = null
                                            }
                                        }
                                    }
                                }
                            }

                            // Update feature selected state (feature = rendered symbol marker)
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.selectedFeatureIdFlow.collect { featureType ->
                                        featureType ?: return@collect

                                        when (featureType) {
                                            is FeatureType.Vessel -> {
                                                if (featureType.id == null) {
                                                    vesselsFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    VESSEL_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }
                                                    style.getSourceAs<GeoJsonSource>(
                                                        VESSEL_GEOJSON_SOURCE
                                                    )
                                                        ?.setGeoJson(
                                                            vesselsFeatureCollection
                                                        )
                                                    removePulsingCircleLayer(style)
                                                } else {
                                                    // Reset size for previously selected features
                                                    vesselsFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    VESSEL_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }

                                                    vesselsFeatureCollection?.features()
                                                        ?.find { feature ->
                                                            feature.getNumberProperty(
                                                                VESSEL_SYMBOL_PROPERTY_ID_KEY,
                                                            ) == featureType.id
                                                        }?.let { feature ->
                                                            feature.properties()
                                                                .addProperty(
                                                                    VESSEL_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    true
                                                                )

                                                            updatePulsingCircle(
                                                                style,
                                                                feature.geometry() as Point
                                                            )

                                                            style.getSourceAs<GeoJsonSource>(
                                                                VESSEL_GEOJSON_SOURCE
                                                            )?.setGeoJson(
                                                                vesselsFeatureCollection
                                                            )
                                                        }
                                                    showVesselInfo(featureType.id.toInt())
                                                }
                                            }

                                            is FeatureType.CustomPoi -> {
                                                if (featureType.id == null) {
                                                    customPoiFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }
                                                    style.getSourceAs<GeoJsonSource>(
                                                        CUSTOM_POI_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        customPoiFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(style)
                                                } else {
                                                    // Reset size for previously selected features
                                                    customPoiFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }

                                                    customPoiFeatureCollection?.features()
                                                        ?.find { feature ->
                                                            feature.getNumberProperty(
                                                                CUSTOM_POI_SYMBOL_PROPERTY_ID_KEY,
                                                            ) == featureType.id
                                                        }?.let { feature ->
                                                            feature.properties()
                                                                .addProperty(
                                                                    CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    true
                                                                )

                                                            updatePulsingCircle(
                                                                style,
                                                                feature.geometry() as Point
                                                            )

                                                            style.getSourceAs<GeoJsonSource>(
                                                                CUSTOM_POI_GEOJSON_SOURCE
                                                            )?.setGeoJson(
                                                                customPoiFeatureCollection
                                                            )
                                                        }
                                                    showCustomPoiInfo(featureType.id.toInt())
                                                }
                                            }

                                            is FeatureType.Harbour -> {
                                                if (featureType.id == null) {
                                                    harboursFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }
                                                    style.getSourceAs<GeoJsonSource>(
                                                        HARBOUR_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        harboursFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(style)
                                                } else {
                                                    // Reset size for previously selected features
                                                    harboursFeatureCollection?.features()
                                                        ?.forEach {
                                                            it.properties()
                                                                .addProperty(
                                                                    HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    false
                                                                )
                                                        }

                                                    harboursFeatureCollection?.features()
                                                        ?.find { feature ->
                                                            feature.getNumberProperty(
                                                                HARBOUR_SYMBOL_PROPERTY_ID_KEY,
                                                            ) == featureType.id
                                                        }?.let { feature ->
                                                            feature.properties()
                                                                .addProperty(
                                                                    HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY,
                                                                    true
                                                                )

                                                            updatePulsingCircle(
                                                                style,
                                                                feature.geometry() as Point
                                                            )

                                                            style.getSourceAs<GeoJsonSource>(
                                                                HARBOUR_GEOJSON_SOURCE
                                                            )?.setGeoJson(
                                                                harboursFeatureCollection
                                                            )
                                                        }
                                                    showHarbourInfo(featureType.id.toInt())
                                                }
                                            }

                                            is FeatureType.Pois -> {
                                                if (featureType.id == null) {
                                                    style.getSourceAs<GeoJsonSource>(
                                                        POI_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        poisFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(style)
                                                } else {

                                                    poisFeatureCollection?.features()
                                                        ?.find { feature ->
                                                            feature.getNumberProperty(
                                                                POI_SYMBOL_PROPERTY_ID_KEY,
                                                            ) == featureType.id
                                                        }?.let { feature ->

                                                            updatePulsingCircle(
                                                                style,
                                                                feature.geometry() as Point
                                                            )

                                                            style.getSourceAs<GeoJsonSource>(
                                                                POI_GEOJSON_SOURCE
                                                            )?.setGeoJson(
                                                                poisFeatureCollection
                                                            )
                                                        }
                                                    showPoiInfo(featureType.id)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Handle geo intent request
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.geoIntentFlow.collect { intentMap ->
                                        intentMap ?: return@collect

                                        intentMap.first.let { latLng ->
                                            mapSharedViewModel.setLatLng(PointType.Marker(latLng))
                                            intentMap.second.let { zoom ->
                                                mapLibreMap.cameraPosition =
                                                    CameraPosition.Builder().target(latLng)
                                                        .zoom(
                                                            when {
                                                                zoom > mapLibre.maxZoomLevel -> mapLibre.maxZoomLevel
                                                                zoom < mapLibre.minZoomLevel -> mapLibre.minZoomLevel
                                                                else -> zoom
                                                            },
                                                        )
                                                        .build()
                                            }
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.anchorageLocationFromHistoryId.collect { id ->
                                        id ?: return@collect
                                        if (!mapSharedViewModel.showAnchorageAlarmBottomSheet.value) return@collect

                                        viewModel.getAnchorageHistoryById(id)?.let { location ->
                                            showAnchorageAlarmBottomSheet(
                                                style = style,
                                                mapLibreMap = mapLibreMap,
                                                latLng = LatLng(
                                                    latitude = location.latitude,
                                                    longitude = location.longitude
                                                ),
                                                anchorageHistoryUiModel = location
                                            )

                                            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.radiusSlider.value =
                                                location.radius.toFloat()
                                        }
                                    }
                                }
                            }

                            // The state of the anchorage alarm bottom sheet should be altered ONLY
                            // by this collector to respect SSOT principle
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.showAnchorageAlarmBottomSheet.collect { show ->
                                        if (show) {
                                            if (anchorageAlarmBottomSheetLayout.visibility == View.VISIBLE) return@collect

                                            val centerPoint =
                                                AnchorageAlarmService.runningAnchorageAlarm.value.takeIf { it.isRunning }
                                                    ?.run {
                                                        LatLng(
                                                            latitude = latitude,
                                                            longitude = longitude
                                                        )
                                                    }
                                                    ?: viewModel.anchorageCenterPoint.value
                                                    ?: mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()

                                            centerPoint?.let {
                                                showAnchorageAlarmBottomSheet(
                                                    style = style,
                                                    mapLibreMap = mapLibreMap,
                                                    latLng = it
                                                )
                                            } ?: run {
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(com.bytecause.core.resources.R.string.could_not_determine_current_position),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                mapSharedViewModel.setShowAnchorageAlarmBottomSheet(
                                                    false
                                                )
                                            }
                                        } else anchorageAlarmBottomSheetBehavior.state =
                                            STATE_HIDDEN
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    AnchorageAlarmService.runningAnchorageAlarm.collect { anchorageAlarm ->
                                        if (anchorageAlarm.isRunning) {
                                            showAnchorageRadius(
                                                style, radiusInMetersToRadiusInPixels(
                                                    mapLibreMap = mapLibreMap,
                                                    radiusInMeters = anchorageAlarm.radius,
                                                    latitude = anchorageAlarm.latitude
                                                ),
                                                latLng = LatLng(
                                                    latitude = anchorageAlarm.latitude,
                                                    longitude = anchorageAlarm.longitude
                                                )
                                            )

                                            binding.mapBottomRightPanelLinearLayout.apply {
                                                mapBottomRightButtonsLinearLayout.visibility =
                                                    View.VISIBLE
                                                anchorageAlarmButton.setOnClickListener {
                                                    mapSharedViewModel.setShowAnchorageAlarmBottomSheet(
                                                        true
                                                    )
                                                    navigateToAnchorageRadius(
                                                        style = style,
                                                        mapLibreMap = mapLibreMap,
                                                        radiusInMeters = anchorageAlarm.radius,
                                                        centerLatitude = anchorageAlarm.latitude,
                                                        centerLongitude = anchorageAlarm.longitude
                                                    )
                                                }
                                            }
                                            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
                                                startButton.isEnabled = false
                                                stopButton.isEnabled = true
                                                radiusSlider.isEnabled = false
                                                moveImageButton.isEnabled = false
                                                radiusSlider.value = anchorageAlarm.radius
                                                radiusValueTextView.text =
                                                    getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                                                        anchorageAlarm.radius.toInt()
                                                            .toString(),
                                                        "M"
                                                    )
                                            }
                                        } else {
                                            binding.mapBottomRightPanelLinearLayout.mapBottomRightButtonsLinearLayout.visibility =
                                                View.GONE
                                            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
                                                if (anchorageAlarmBottomSheetBehavior.state == STATE_HIDDEN) {
                                                    removeAnchorageRadius(style)
                                                }

                                                startButton.isEnabled = true
                                                stopButton.isEnabled = false
                                                radiusSlider.isEnabled = true
                                                moveImageButton.isEnabled = true
                                            }
                                        }
                                    }
                                }
                            }

                            fun drawAnchorLine(currentPosition: LatLng) {
                                val (latitude, longitude) = if (AnchorageAlarmService.runningAnchorageAlarm.value.isRunning) {
                                    AnchorageAlarmService.runningAnchorageAlarm.value.run { latitude to longitude }
                                } else viewModel.anchorageCenterPoint.value?.run { latitude to longitude }
                                    ?: run {
                                        currentPosition.latitude to currentPosition.longitude
                                    }

                                if (style.getSourceAs<GeoJsonSource>(
                                        ANCHOR_CHAIN_LINE_GEOJSON_SOURCE
                                    ) != null
                                ) {
                                    style.apply {
                                        removeLayer(ANCHOR_CHAIN_LINE_LAYER)
                                        removeSource(
                                            ANCHOR_CHAIN_LINE_GEOJSON_SOURCE
                                        )
                                    }
                                }

                                val lineFeatures = Feature.fromGeometry(
                                    LineString.fromLngLats(
                                        listOf(
                                            Point.fromLngLat(
                                                currentPosition.longitude,
                                                currentPosition.latitude
                                            ),
                                            Point.fromLngLat(
                                                longitude,
                                                latitude
                                            )
                                        )
                                    )
                                )

                                val lineSource = GeoJsonSource(
                                    ANCHOR_CHAIN_LINE_GEOJSON_SOURCE,
                                    FeatureCollection.fromFeature(lineFeatures)
                                )

                                val lineLayer = LineLayer(
                                    ANCHOR_CHAIN_LINE_LAYER,
                                    ANCHOR_CHAIN_LINE_GEOJSON_SOURCE
                                ).apply {
                                    setProperties(
                                        lineDasharray(arrayOf(4f, 3f)),
                                        lineColor(
                                            requireContext().getColor(com.bytecause.core.resources.R.color.red)
                                                .toHexString()
                                        ),
                                        lineWidth(2f)
                                    )
                                }

                                style.apply {
                                    addSource(lineSource)
                                    addLayerBelow(
                                        lineLayer,
                                        ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER
                                    )
                                }

                                updateVesselDistanceFromAnchor(
                                    currentPosition.distanceTo(
                                        LatLng(latitude, longitude)
                                    )
                                )
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.lastKnownPosition.collect { latLng ->
                                        latLng ?: return@collect

                                        if (AnchorageAlarmService.runningAnchorageAlarm.value.isRunning ||
                                            anchorageAlarmBottomSheetBehavior.state == STATE_EXPANDED
                                        ) {
                                            drawAnchorLine(latLng)
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.trackedPoints.collect { points ->
                                        if (points.isEmpty()) {
                                            mapStyle.apply {
                                                getSourceAs<GeoJsonSource>(
                                                    ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE
                                                )?.let {
                                                    removeLayer(ANCHORAGE_MOVEMENT_LINE_LAYER)
                                                    removeSource(it.id)
                                                }
                                            }

                                            return@collect
                                        }

                                        val feature =
                                            Feature.fromGeometry(LineString.fromLngLats(points))

                                        mapStyle.getSourceAs<GeoJsonSource>(
                                            ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE
                                        )?.setGeoJson(feature) ?: run {
                                            val lineSource = GeoJsonSource(
                                                ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE,
                                                FeatureCollection.fromFeature(feature)
                                            )

                                            val lineLayer = LineLayer(
                                                ANCHORAGE_MOVEMENT_LINE_LAYER,
                                                ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE
                                            ).apply {
                                                setProperties(
                                                    lineColor(
                                                        requireContext().getColor(com.bytecause.core.resources.R.color.yellow)
                                                            .toHexString()
                                                    ),
                                                    lineWidth(2f)
                                                )
                                            }

                                            style.apply {
                                                addSource(lineSource)
                                                addLayerBelow(
                                                    lineLayer,
                                                    ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (mapSharedViewModel.geoIntentFlow.replayCache.lastOrNull() == null) {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    // if anchorage alarm is on, navigate to anchorage location,
                                    // otherwise use cached position
                                    AnchorageAlarmService.runningAnchorageAlarm.value.takeIf { it.isRunning }
                                        ?.run {
                                            navigateToAnchorageRadius(
                                                style = style,
                                                mapLibreMap = mapLibreMap,
                                                radiusInMeters = radius,
                                                centerLatitude = latitude,
                                                centerLongitude = longitude
                                            )
                                        } ?: run {
                                        cameraPosition = mapSharedViewModel.cameraPosition
                                            ?: viewModel.getUserLocation().firstOrNull()?.let {
                                                CameraPosition.Builder().target(it)
                                                    .zoom(ZOOM_IN_DEFAULT_LEVEL)
                                                    .build()
                                            }
                                                    ?: CameraPosition.Builder()
                                                .target(LatLng(0.0, 0.0))
                                                .zoom(1.0)
                                                .build()
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.expandedAnchorageRepositionType.collect { type ->
                                        val viewScope =
                                            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetRepositionLayout

                                        when (type) {
                                            AnchorageRepositionType.Coordinates -> {
                                                viewScope.apply {
                                                    repositionByCoordinatesLinearLayoutContent.visibility =
                                                        View.VISIBLE
                                                    repositionManuallyConstraintLayoutContent.visibility =
                                                        View.GONE
                                                    repositionByDistanceLinearLayoutContent.visibility =
                                                        View.GONE
                                                }
                                            }

                                            AnchorageRepositionType.Manual -> {
                                                viewScope.apply {
                                                    repositionManuallyConstraintLayoutContent.visibility =
                                                        View.VISIBLE
                                                    repositionByDistanceLinearLayoutContent.visibility =
                                                        View.GONE
                                                    repositionByCoordinatesLinearLayoutContent.visibility =
                                                        View.GONE
                                                }
                                            }

                                            AnchorageRepositionType.Distance -> {
                                                viewScope.apply {
                                                    repositionByDistanceLinearLayoutContent.visibility =
                                                        View.VISIBLE
                                                    repositionManuallyConstraintLayoutContent.visibility =
                                                        View.GONE
                                                    repositionByCoordinatesLinearLayoutContent.visibility =
                                                        View.GONE
                                                }
                                            }

                                            null -> {
                                                viewScope.apply {
                                                    repositionByDistanceLinearLayoutContent.visibility =
                                                        View.GONE
                                                    repositionManuallyConstraintLayoutContent.visibility =
                                                        View.GONE
                                                    repositionByCoordinatesLinearLayoutContent.visibility =
                                                        View.GONE
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.anchorageCenterPoint.collect { latLng ->
                                        latLng ?: return@collect

                                        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                            ?.let {
                                                drawAnchorLine(it)
                                            }

                                        moveAnchorageRadius(
                                            style,
                                            latLng
                                        )

                                        updateAnchorageRadiusCenterPositionTextView(
                                            latLng
                                        )
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.isAnchorageRepositionEnabled.collect { enabled ->
                                        if (enabled) {
                                            binding.anchorageAlarmBottomSheet.apply {
                                                anchorageAlarmBottomSheetMainContentId.root.visibility =
                                                    View.GONE
                                                anchorageAlarmBottomSheetRepositionLayout.apply innerApply@{
                                                    root.visibility = View.VISIBLE

                                                    updateAnchorageRadiusCenterPositionTextView(
                                                        viewModel.anchorageCenterPoint.value!!
                                                    )

                                                    anchorageRepositionCancelButton.setOnClickListener {
                                                        viewModel.setIsAnchorageRepositionEnabled(
                                                            false
                                                        )
                                                        unregisterSensors()
                                                    }

                                                    anchorageCoordinatesRepositionDoneButton.setOnClickListener {
                                                        val (latitudeText, longitudeText) = latitudeEditText.text.toString() to longitudeEditText.text.toString()

                                                        if (latitudeText.isBlank() || longitudeText.isBlank()) {
                                                            if (latitudeText.isBlank()) {
                                                                latitudeEditText.error =
                                                                    getString(com.bytecause.core.resources.R.string.cannot_be_empty)
                                                            }

                                                            if (longitudeText.isBlank()) {
                                                                longitudeEditText.error =
                                                                    getString(com.bytecause.core.resources.R.string.cannot_be_empty)
                                                            }
                                                            return@setOnClickListener
                                                        }

                                                        val latitude = latitudeText.toDouble()
                                                        val longitude = longitudeText.toDouble()

                                                        areCoordinatesValid(
                                                            latitude,
                                                            longitude
                                                        ).let {
                                                            if (!it) {
                                                                coordinatesOutOfBoundsErrorMessage.visibility =
                                                                    View.VISIBLE
                                                                return@setOnClickListener
                                                            } else {
                                                                if (coordinatesOutOfBoundsErrorMessage.visibility == View.VISIBLE) {
                                                                    coordinatesOutOfBoundsErrorMessage.visibility =
                                                                        View.GONE
                                                                }
                                                            }
                                                        }

                                                        val updatedAnchoragePosition =
                                                            LatLng(
                                                                latitude,
                                                                longitude
                                                            )

                                                        viewModel.setAnchorageCenterPoint(
                                                            updatedAnchoragePosition
                                                        )
                                                        viewModel.setExpandedAnchorageRepositionType(
                                                            null
                                                        )
                                                        resetAnchorageRepositionViewState()
                                                    }

                                                    anchorageDistanceRepositionDoneButton.setOnClickListener {
                                                        val currentPosition =
                                                            mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                                                ?: return@setOnClickListener

                                                        val (distanceText, bearingText) = distanceEditText.text.toString() to bearingEditText.text.toString()

                                                        if (distanceText.isBlank() || bearingText.isBlank()) {
                                                            if (distanceText.isBlank()) {
                                                                distanceEditText.error = getString(
                                                                    com.bytecause.core.resources.R.string.cannot_be_empty
                                                                )
                                                            }
                                                            if (bearingText.isBlank()) {
                                                                bearingEditText.error = getString(
                                                                    com.bytecause.core.resources.R.string.cannot_be_empty
                                                                )
                                                            }
                                                            return@setOnClickListener
                                                        }

                                                        val (distance, bearing) = distanceText
                                                            .toDouble() to bearingText.toDouble()
                                                        val updatedAnchoragePosition =
                                                            currentPosition.newLatLngFromDistance(
                                                                distance = distance,
                                                                bearing = bearing
                                                            )

                                                        viewModel.setAnchorageCenterPoint(
                                                            updatedAnchoragePosition
                                                        )
                                                        viewModel.setExpandedAnchorageRepositionType(
                                                            null
                                                        )
                                                        unregisterSensors()
                                                        resetAnchorageRepositionViewState()
                                                    }

                                                    currentPositionImageButton.setOnClickListener {
                                                        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                                            ?.run {
                                                                latitudeEditText.setText(latitude.toString())
                                                                longitudeEditText.setText(longitude.toString())
                                                            }
                                                    }

                                                    moveUp.setOnClickListener {
                                                        viewModel.anchorageCenterPoint.value?.run {
                                                            viewModel.setAnchorageCenterPoint(
                                                                LatLng(
                                                                    latitude = latitude + ANCHORAGE_RADIUS_MOVE_BY,
                                                                    longitude = longitude
                                                                )
                                                            )
                                                        }
                                                    }

                                                    getBearingFromCompassImageButton.setOnClickListener {
                                                        sensorManager?.let {
                                                            unregisterSensors()

                                                            getBearingFromCompassImageButton.setBackgroundColor(
                                                                Color.TRANSPARENT
                                                            )
                                                        } ?: run {
                                                            sensorManager =
                                                                requireActivity().getSystemService(
                                                                    SENSOR_SERVICE
                                                                ) as SensorManager


                                                            sensorManager?.run {
                                                                accelerometer =
                                                                    getDefaultSensor(
                                                                        TYPE_ACCELEROMETER
                                                                    )
                                                                magnetometer =
                                                                    getDefaultSensor(
                                                                        TYPE_MAGNETIC_FIELD
                                                                    )

                                                                registerListener(
                                                                    sensorListener,
                                                                    accelerometer,
                                                                    SensorManager.SENSOR_DELAY_UI
                                                                )
                                                                registerListener(
                                                                    sensorListener,
                                                                    magnetometer,
                                                                    SensorManager.SENSOR_DELAY_UI
                                                                )
                                                            }

                                                            getBearingFromCompassImageButton.background =
                                                                ContextCompat.getDrawable(
                                                                    requireContext(),
                                                                    com.bytecause.core.resources.R.drawable.rounded_button_background
                                                                )
                                                        }
                                                    }

                                                    moveDown.setOnClickListener {
                                                        viewModel.anchorageCenterPoint.value?.run {
                                                            viewModel.setAnchorageCenterPoint(
                                                                LatLng(
                                                                    latitude = latitude - ANCHORAGE_RADIUS_MOVE_BY,
                                                                    longitude = longitude
                                                                )
                                                            )
                                                        }
                                                    }

                                                    moveLeft.setOnClickListener {
                                                        viewModel.anchorageCenterPoint.value?.run {
                                                            viewModel.setAnchorageCenterPoint(
                                                                LatLng(
                                                                    latitude = latitude,
                                                                    longitude = longitude - ANCHORAGE_RADIUS_MOVE_BY
                                                                )
                                                            )
                                                        }
                                                    }

                                                    moveRight.setOnClickListener {
                                                        viewModel.anchorageCenterPoint.value?.run {
                                                            viewModel.setAnchorageCenterPoint(
                                                                LatLng(
                                                                    latitude = latitude,
                                                                    longitude = longitude + ANCHORAGE_RADIUS_MOVE_BY
                                                                )
                                                            )
                                                        }
                                                    }

                                                    repositionByCoordinatesCardView.setOnClickListener {
                                                        viewModel.setExpandedAnchorageRepositionType(
                                                            AnchorageRepositionType.Coordinates
                                                        )
                                                    }

                                                    manualRepostionCardView.setOnClickListener {
                                                        viewModel.setExpandedAnchorageRepositionType(
                                                            AnchorageRepositionType.Manual
                                                        )
                                                    }

                                                    repositionByDistanceFromAnchorCardView.setOnClickListener {
                                                        viewModel.setExpandedAnchorageRepositionType(
                                                            AnchorageRepositionType.Distance
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            resetAnchorageRepositionViewState()
                                            binding.anchorageAlarmBottomSheet.apply {
                                                KeyboardUtils.forceCloseKeyboard(
                                                    anchorageAlarmBottomSheetRepositionLayout.root
                                                )
                                                anchorageAlarmBottomSheetMainContentId.root.visibility =
                                                    View.VISIBLE
                                                anchorageAlarmBottomSheetRepositionLayout.root.visibility =
                                                    View.GONE
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        addOnMapClickListener { latLng ->
                            handleSymbolClick(latLng)
                            closeBottomSheetLayout()
                            true
                        }

                        addOnCameraIdleListener {
                            if (!viewModel.selectedPoiCategories.value.isNullOrEmpty()) {
                                if ((viewModel.selectedPoiCategories.value?.size ?: 0) > 30) {
                                    if (mapLibreMap.cameraPosition.zoom > POIS_VISIBILITY_ZOOM_LEVEL) viewModel.updatePoiBbox(
                                        mapLibreMap.projection.visibleRegion.latLngBounds
                                    )
                                    else viewModel.updatePoiBbox(null)
                                } else viewModel.updatePoiBbox(
                                    mapLibreMap.projection.visibleRegion.latLngBounds
                                )
                            }

                            if (viewModel.isAisActivated.value) viewModel.updateVesselBbox(
                                mapLibreMap.projection.visibleRegion.latLngBounds
                            )
                            if (viewModel.areHarboursVisible.value) viewModel.updateHarbourBbox(
                                mapLibreMap.projection.visibleRegion.latLngBounds
                            )
                            if (viewModel.areAnchoragesVisible.value) viewModel.updateAnchoragesBbox(
                                mapLibreMap.projection.visibleRegion.latLngBounds
                            )
                        }

                        addOnCameraMoveListener {
                            // if measure mode is on, render new dashed line which points
                            // to the center of the screen on each camera move
                            if (viewModel.isMeasuring) {
                                mapLibreMap.cameraPosition.target?.let { centerPoint ->
                                    val feature = Feature.fromGeometry(
                                        LineString.fromLngLats(
                                            viewModel.measurePointsSharedFlow.replayCache.lastOrNull()
                                                ?.map {
                                                    Point.fromLngLat(
                                                        it.longitude,
                                                        it.latitude
                                                    )
                                                }
                                                ?.plus(
                                                    Point.fromLngLat(
                                                        centerPoint.longitude,
                                                        centerPoint.latitude
                                                    )
                                                )
                                                ?: emptyList()
                                        )
                                    )

                                    mapStyle.getSourceAs<GeoJsonSource>(
                                        MEASURE_LINE_GEOJSON_SOURCE
                                    )?.setGeoJson(feature)
                                }
                            }
                        }

                        addOnMoveListener(
                            object : OnMoveListener {
                                override fun onMove(detector: MoveGestureDetector) {
                                    if (!locationComponent.isLocationComponentActivated) return
                                    if (locationComponent.cameraMode == DEFAULT_BUTTON_STATE) return
                                    if (viewModel.locationButtonStateFlow.value != DEFAULT_BUTTON_STATE) {
                                        viewModel.setLocationButtonState(DEFAULT_BUTTON_STATE)
                                    }
                                }

                                override fun onMoveBegin(detector: MoveGestureDetector) {}
                                override fun onMoveEnd(detector: MoveGestureDetector) {}
                            },
                        )

                        addOnMapLongClickListener {
                            mapSharedViewModel.setLatLng(PointType.Marker(it))
                            true
                        }

                        uiSettings.apply {
                            compassGravity = Gravity.START

                            val location = IntArray(2)
                            binding.mapTopLeftPanelLinearLayout.leftLinearLayout.getLocationOnScreen(
                                location
                            )
                            val leftLinearLayoutBottom =
                                location[1] + binding.mapTopLeftPanelLinearLayout.leftLinearLayout.height / 1.7f

                            setCompassMargins(
                                location[0] / 2,
                                leftLinearLayoutBottom.toInt(),
                                0,
                                0,
                            )
                        }
                    }
                }
            }
        }

        // Get last user's position from SharedPreferences on first start if present.
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getUserLocation().firstOrNull()?.let {
                if (mapSharedViewModel.lastKnownPosition.replayCache.firstOrNull() != null) return@let
                mapSharedViewModel.setLastKnownPosition(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchBoxTextPlaceholder.collect { textList ->
                    val last = textList.takeIf { it.isNotEmpty() }?.last()

                    binding.searchMapBox.searchMapEditText.setText(
                        (last as? SearchBoxTextType.Coordinates)?.text
                            ?: (last as? SearchBoxTextType.PoiName)?.text
                    )
                }
            }
        }

        // Search box settings.
        binding.searchMapBox.searchMapEditText.apply {
            setOnClickListener {
                if (!lastClick.lastClick(1000)) return@setOnClickListener
                findNavController().navigateToSearchNavigation()
            }

            setOnTextChangedListener(
                object :
                    CustomTextInputEditText.OnTextChangedListener {
                    override fun onTextChanged(text: CharSequence?) {
                        if (!text.isNullOrEmpty()) {
                            ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.baseline_close_24,
                            )?.let {
                                if (drawableList.contains(it)) return
                                it.setTint(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        com.bytecause.core.resources.R.color.black
                                    )
                                )
                                setDrawables(right = it)
                            }
                        } else {
                            setDrawables(right = null)
                        }
                    }
                },
            )

            setOnDrawableClickListener(
                object :
                    CustomTextInputEditText.OnDrawableClickListener {
                    override fun onStartDrawableClick(view: CustomTextInputEditText) {
                        (activity as? DrawerController)?.toggleDrawer()
                    }

                    override fun onEndDrawableClick(view: CustomTextInputEditText) {
                        if (mapSharedViewModel.showPoiStateFlow.value != null) {
                            mapSharedViewModel.setPoiToShow(null)
                        }

                        removeMarker()
                        removeSearchPlace()

                        when {
                            bottomSheetBehavior.state == STATE_EXPANDED -> {
                                closeBottomSheetLayout()
                            }

                            markerBottomSheetBehavior.state == STATE_EXPANDED -> {
                                closeMarkerBottomSheetLayout()
                            }
                        }
                    }
                },
            )

            setDrawables(
                ContextCompat.getDrawable(
                    requireContext(),
                    com.bytecause.core.resources.R.drawable.baseline_menu_24,
                ),
                null,
            )

            setPaddingRelative(
                30, // Left padding
                paddingTop,
                paddingEnd,
                paddingBottom,
            )
            compoundDrawablePadding = 30

            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
            isLongClickable = false
        }

        binding.mapTopRightPanelLinearLayout.locationButton.setOnClickListener {
            if (!lastClick.lastClick(300)) return@setOnClickListener

            if (requireContext().isLocationPermissionGranted()) {
                if (locationButtonState == DEFAULT_BUTTON_STATE && isCentered() && mapLibre.cameraPosition.zoom == ZOOM_IN_DEFAULT_LEVEL) {
                    viewModel.setLocationButtonState(TRACKING_BUTTON_STATE)
                } else {
                    animateCameraToLocation()
                }
            } else {
                activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.mapTopRightPanelLinearLayout.layersButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_mapFragment_to_mapBottomSheetFragment)
        }

        binding.mapTopLeftPanelLinearLayout.customizeMapImageButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_mapFragment_to_customizeMapDialog)
        }

        binding.bottomSheetId.addMarkerButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigateToCustomPoiNavigation()
        }

        binding.bottomSheetId.measureDistanceButton.setOnClickListener {
            enterMeasureDistanceMode()
        }

        binding.measureDistanceTop.measureDistanceBackButton.setOnClickListener {
            leaveMeasureDistanceMode()
        }

        binding.measureDistanceTop.undoDistanceLine.setOnClickListener {
            viewModel.measurePointsSharedFlow.replayCache.last().let {
                if (it.size > 1) {
                    viewModel.removeMeasurePoint(it.last())
                }
            }
        }

        binding.measureDistanceTop.clearDistanceLines.setOnClickListener {
            viewModel.measurePointsSharedFlow.replayCache.last().let {
                if (it.size > 1) {
                    viewModel.clearMeasurePoints()
                }
            }
        }

        binding.measureDistanceBottomSheet.addMeasurePointButton.setOnClickListener {
            addMeasurePoint()
        }

        binding.bottomSheetId.shareLocationButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener

            mapSharedViewModel.latLngFlow.value?.let { pointType ->
                val point = pointType as PointType.Marker

                val action =
                    MapFragmentDirections.actionMapFragmentToMapShareBottomSheetDialog(
                        floatArrayOf(
                            point.latLng.latitude.toFloat(),
                            point.latLng.longitude.toFloat(),
                        ),
                    )
                findNavController().navigate(action)
            }
        }

        binding.bottomSheetId.toolsButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            hideBottomSheetLayout()
            findNavController().navigate(R.id.action_mapFragment_to_mapToolsBottomSheetFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.tileSource.collect { tileSource ->
                    if (!::mapLibre.isInitialized || tileSource == null) return@collect

                    (tileSource as? TileSources.Raster)?.let {
                        TileSourceLoader.loadRasterTileSource(
                            mapStyle,
                            tileSource = tileSource
                        )
                    }

                    if (!mapLibre.locationComponent.isLocationComponentActivated) {
                        activateLocationComponent(mapStyle)
                    }
                }
            }
        }

        if (!isLocationPermissionGranted) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mapSharedViewModel.permissionGranted.collect { granted ->
                        granted ?: return@collect

                        if (granted) {
                            isLocationPermissionGranted = granted
                        }
                    }
                }
            }
        }

        if (viewModel.isMeasuring) {
            enterMeasureDistanceMode()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.isCustomizeDialogVisible.collect {
                    when (it) {
                        true -> {
                            binding.searchMapBox.searchBoxLinearLayout.visibility =
                                View.GONE
                            binding.mapTopLeftPanelLinearLayout.leftLinearLayout.visibility =
                                View.GONE
                            binding.mapTopRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
                                View.GONE
                        }

                        false -> {
                            binding.searchMapBox.searchBoxLinearLayout.visibility =
                                View.VISIBLE
                            binding.mapTopLeftPanelLinearLayout.leftLinearLayout.visibility =
                                View.VISIBLE
                            binding.mapTopRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
                                View.VISIBLE
                        }
                    }
                }
            }
        }

        // Changes location button state, if state == 1 mapView will be rotated based on current
        // device's direction.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationButtonStateFlow.collect {
                    it ?: return@collect

                    locationButtonState = it
                    setEnabledLocationDrawable()
                }
            }
        }
    }

    private fun showAnchorageRadius(style: Style, radiusInPixels: Float, latLng: LatLng) {
        if (style.getSourceAs<GeoJsonSource>(ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE) != null) {
            return
        }

        val radiusBorderSource = GeoJsonSource(ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE).apply {
            setGeoJson(
                Point.fromLngLat(
                    latLng.longitude,
                    latLng.latitude
                )
            )
        }

        val radiusBorderLayer =
            CircleLayer(ANCHORAGE_BORDER_RADIUS_LAYER, ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE)
                .withProperties(
                    circleRadius(radiusInPixels),
                    circleColor(Color.DKGRAY),
                    circleOpacity(0.2f),
                    circleStrokeWidth(2f),
                    circleStrokeColor(Color.BLACK)
                )

        val centerRadiusSource =
            GeoJsonSource(ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE).apply {
                setGeoJson(
                    Point.fromLngLat(
                        latLng.longitude,
                        latLng.latitude
                    )
                )
            }

        val centerRadiusLayer = SymbolLayer(
            ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER,
            ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE
        )
            .withProperties(
                iconImage(ANCHORAGE_CENTER_SYMBOL_ICON)
            )

        ContextCompat.getDrawable(
            requireContext(),
            com.bytecause.core.resources.R.drawable.anchor
        )?.let {
            style.addImage(ANCHORAGE_CENTER_SYMBOL_ICON, it)
        }

        style.apply {
            addSource(radiusBorderSource)
            addSource(centerRadiusSource)

            addLayerAbove(radiusBorderLayer, MAIN_LAYER_ID)
            addLayerAbove(centerRadiusLayer, ANCHORAGE_BORDER_RADIUS_LAYER)
        }

        // we need camera move listener to calculate new radius in pixels on each zoom change
        mapLibre.addOnCameraMoveListener(onCameraMoveListener)
    }

    private fun updateAnchorageRadius(style: Style, latitude: Double) {
        val circleLayer = style.getLayerAs<CircleLayer>(ANCHORAGE_BORDER_RADIUS_LAYER)

        // Update the CircleLayer properties
        circleLayer?.setProperties(
            circleRadius(
                radiusInMetersToRadiusInPixels(
                    mapLibreMap = mapLibre,
                    radiusInMeters = binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.radiusSlider.value,
                    latitude = latitude
                )
            )
        )
    }

    private fun moveAnchorageRadius(style: Style, latLng: LatLng) {
        style.apply {
            val centerPoint =
                getSourceAs<GeoJsonSource>(ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE)
            val borderRadius = getSourceAs<GeoJsonSource>(ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE)

            centerPoint?.setGeoJson(Point.fromLngLat(latLng.longitude, latLng.latitude))
            borderRadius?.setGeoJson(Point.fromLngLat(latLng.longitude, latLng.latitude))
        }
    }

    private fun removeAnchorageRadius(style: Style) {
        style.apply {
            removeLayer(ANCHORAGE_BORDER_RADIUS_LAYER)
            removeLayer(ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER)
            removeSource(ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE)
            removeSource(ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE)

            removeLayer(ANCHOR_CHAIN_LINE_LAYER)
            removeSource(ANCHOR_CHAIN_LINE_GEOJSON_SOURCE)
        }

        viewModel.setAnchorageCenterPoint(null)
        mapLibre.removeOnCameraMoveListener(onCameraMoveListener)
    }

    // Adds a layer on which to render the tapped animation.
    private fun addPulsingCircleLayer(style: Style) {
        val circleSource = GeoJsonSource(PULSING_CIRCLE_GEOJSON_SOURCE)
        style.addSource(circleSource)

        val circleLayer =
            CircleLayer(PULSING_CIRCLE_LAYER, PULSING_CIRCLE_GEOJSON_SOURCE)
                .withProperties(
                    circleColor(ANIMATED_CIRCLE_COLOR)
                )

        style.addLayerBelow(circleLayer, VESSEL_SYMBOL_LAYER)
    }

    // Removes layer reserved for tapped animation and cancels animation.
    private fun removePulsingCircleLayer(style: Style) {
        style.apply {
            removeLayer(PULSING_CIRCLE_LAYER)
            removeSource(PULSING_CIRCLE_GEOJSON_SOURCE)
        }
        circleLayerAnimator?.cancel()
        circleLayerAnimator = null
    }

    // Change position of this tapped animation layer.
    private fun updatePulsingCircle(style: Style, point: Point) {
        val source = style.getSourceAs(PULSING_CIRCLE_GEOJSON_SOURCE) ?: run {
            // if source doesn't exist add new source and layer
            addPulsingCircleLayer(style)
            // return new source
            style.getSourceAs<GeoJsonSource>(PULSING_CIRCLE_GEOJSON_SOURCE)
        }
        source?.apply {
            setGeoJson(point)
            animatePulsingCircle(style)
        }
    }

    // Renders tapped pulsing animation.
    private fun animatePulsingCircle(style: Style) {
        circleLayerAnimator?.cancel()
        val circleLayer =
            style.getLayerAs<CircleLayer>(PULSING_CIRCLE_LAYER)

        circleLayerAnimator = ValueAnimator.ofFloat(0f, ANIMATED_CIRCLE_RADIUS).apply {
            duration = PULSING_CIRCLE_ANIMATION_DURATION
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float

                if (value < 14.5f) {
                    // Circle is expanding show the circle
                    circleLayer?.setProperties(
                        circleRadius(value),
                        circleOpacity(1f - (value / ANIMATED_CIRCLE_RADIUS)),
                    )
                } else {
                    // Value is greater than 14.5f, circle is shrinking, hide circle to prevent transition glitch
                    circleLayer?.setProperties(
                        circleRadius(0f),
                        circleOpacity(0f),
                    )
                }
            }
        }
        circleLayerAnimator?.start()
    }

    // Interferes all taps on the map and determines if a symbol is tapped.
    private fun handleSymbolClick(latLng: LatLng) {
        if (markerBottomSheetBehavior.state == STATE_EXPANDED) {
            closeMarkerBottomSheetLayout()
        }

        val screenPoint = mapLibre.projection.toScreenLocation(latLng)
        val features = mapLibre.queryRenderedFeatures(
            screenPoint,
            VESSEL_SYMBOL_LAYER,
            HARBOUR_SYMBOL_LAYER,
            CUSTOM_POI_SYMBOL_LAYER,
            POI_SYMBOL_LAYER
        )

        if (features.isNotEmpty()) {
            val selectedFeature = features.first()
            val symbolType = selectedFeature.getStringProperty(SYMBOL_TYPE)

            when (symbolType) {
                FeatureTypeEnum.VESSEL.name -> {
                    val vesselId =
                        selectedFeature.getNumberProperty(VESSEL_SYMBOL_PROPERTY_ID_KEY)

                    viewModel.setSelectedFeatureId(FeatureType.Vessel(vesselId.toLong()))
                }

                FeatureTypeEnum.CUSTOM_POI.name -> {
                    val customPoiId =
                        selectedFeature.getNumberProperty(CUSTOM_POI_SYMBOL_PROPERTY_ID_KEY)

                    viewModel.setSelectedFeatureId(FeatureType.CustomPoi(customPoiId.toLong()))
                }

                FeatureTypeEnum.HARBOUR.name -> {
                    val harbourId =
                        selectedFeature.getNumberProperty(HARBOUR_SYMBOL_PROPERTY_ID_KEY)

                    viewModel.setSelectedFeatureId(FeatureType.Harbour(harbourId.toLong()))
                }

                FeatureTypeEnum.POIS.name -> {
                    val poiId =
                        selectedFeature.getNumberProperty(POI_SYMBOL_PROPERTY_ID_KEY)

                    viewModel.setSelectedFeatureId(FeatureType.Pois(poiId.toLong()))
                }
            }
        }
    }

    // Gets information about custom poi from the database and pass this state into showMarkerBottomSheet,
// which will render this state.
    private fun showCustomPoiInfo(customPoiId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchCustomPoiById(customPoiId).firstOrNull()?.let { customPoi ->
                showMarkerBottomSheet(
                    MarkerInfoModel(
                        title = customPoi.poiName,
                        iconImage = ContextCompat.getDrawable(
                            requireContext(),
                            resources.getIdentifier(
                                customPoi.drawableResourceName,
                                "drawable",
                                requireContext().packageName
                            )
                        ),
                        description = customPoi.description.takeIf { it.isNotBlank() },
                        position = LatLng(customPoi.latitude, customPoi.longitude)
                    )
                )
            }
        }
    }

    private fun showPoiInfo(poiId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchPoiWithInfoById(poiId).firstOrNull()?.let { poi ->
                val title = poi.tags["name"] ?: formatTagString(poi.category)

                showMarkerBottomSheet(
                    MarkerInfoModel(
                        title = title ?: "",
                        type = formatTagString(
                            getPoiType(poi.tags)
                                ?: poi.category
                        ).takeIf {
                            title != null && title != formatTagString(
                                it
                            )
                        }
                            ?.let { "($it)" },
                        iconImage = null,
                        propImages = extractPropImagesFromTags(poi.tags),
                        description = excludeDescriptionKeysFromTags(poi.tags).takeIf { it.isNotBlank() },
                        contacts = extractContactsFromTags(poi.tags).takeIf { it.isNotBlank() },
                        image = replaceHttpWithHttps(poi.tags["image"]),
                        position = LatLng(poi.latitude, poi.longitude)
                    )
                )
            }
        }
    }

    // Gets information about vessel from the database and pass this state into showMarkerBottomSheet,
// which will render this state.
    private fun showVesselInfo(vesselId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchVesselById(vesselId).firstOrNull()?.let { vesselInfoEntity ->

                val vesselPosition =
                    LatLng(
                        vesselInfoEntity.latitude.toDouble(),
                        vesselInfoEntity.longitude.toDouble(),
                    )

                val sdf =
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault(),
                    ).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                val locale = Locale("", vesselInfoEntity.flag)

                showMarkerBottomSheet(
                    MarkerInfoModel(
                        title = resources.getString(com.bytecause.core.resources.R.string.vessel_title)
                            .format(
                                vesselInfoEntity.name,
                                vesselInfoEntity.flag
                            ),
                        iconImage = ContextCompat.getDrawable(
                            requireContext(),
                            com.bytecause.core.resources.R.drawable.vessel_marker
                        )
                            ?.apply {
                                this.setTint(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        MapUtil.determineVesselColorType(vesselInfoEntity.type),
                                    ),
                                )
                            },
                        description = resources.getString(com.bytecause.core.resources.R.string.vessel_info_description)
                            .format(
                                locale.displayCountry,
                                com.bytecause.util.string.StringUtil.formatNumberWithSpaces(
                                    vesselInfoEntity.mmsi.toInt()
                                ),
                                when (vesselInfoEntity.length.isEmpty()) {
                                    true -> "--"
                                    else -> vesselInfoEntity.length + " m"
                                },
                                (
                                        vesselInfoEntity.speed.takeIf { speed -> speed.isNotEmpty() }
                                            ?.toDouble()
                                            ?.div(10.0)
                                        ) ?: "--",
                                if (vesselInfoEntity.heading.isEmpty()) "--" else vesselInfoEntity.heading + "°",
                                getString(
                                    MapUtil.determineVesselType(
                                        vesselInfoEntity.type,
                                    ),
                                ),
                                if (vesselInfoEntity.eta.isNotEmpty()) {
                                    sdf.format(
                                        vesselInfoEntity.timeStamp - (vesselInfoEntity.eta.toLong() * 60000L),
                                    )
                                } else {
                                    "--"
                                },
                            ),
                        position = vesselPosition
                    )
                )
            }
        }
    }

    // Gets information about harbour from the database and pass this state into showMarkerBottomSheet,
// which will render this state.
    private fun showHarbourInfo(harbourId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchHarbourById(harbourId).firstOrNull()?.let { harbour ->
                showMarkerBottomSheet(
                    MarkerInfoModel(
                        title = harbour.tags["name"] ?: formatTagString(harbour.tags["leisure"])
                        ?: "",
                        iconImage = null,
                        propImages = extractPropImagesFromTags(harbour.tags),
                        description = excludeDescriptionKeysFromTags(harbour.tags).takeIf { it.isNotBlank() },
                        image = replaceHttpWithHttps(harbour.tags["image"]),
                        position = LatLng(harbour.latitude, harbour.longitude)
                    )
                )
            }
        }
    }

    // when POI marker is hidden after bottom sheet expansion, camera will be adjusted.
    private fun adjustMapViewPositionIfNeeded(bottomSheetLayout: LinearLayout, point: LatLng) {
        bottomSheetLayout.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    bottomSheetLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val bottomSheetHeight = bottomSheetLayout.height
                    val mapViewHeight = mapView!!.height

                    val projection = mapLibre.projection
                    val symbolScreenPoint = projection.toScreenLocation(point)
                    val range = Range.create(mapViewHeight - bottomSheetHeight, mapViewHeight)

                    if (range.contains(symbolScreenPoint.y.toInt())) {
                        val estimatedCenter = mapViewHeight - bottomSheetHeight

                        symbolScreenPoint.y += estimatedCenter / 4

                        val newLatLng = projection.fromScreenLocation(symbolScreenPoint)

                        mapLibre.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                newLatLng,
                                mapLibre.cameraPosition.zoom
                            ),
                        )
                    }
                }
            }
        )
    }

    private fun navigateToAnchorageRadius(
        style: Style,
        mapLibreMap: MapLibreMap,
        radiusInMeters: Float,
        centerLatitude: Double,
        centerLongitude: Double
    ) {
        val bounds = calculateBoundsForRadius(
            radiusInMeters = radiusInMeters,
            centerLatitude = centerLatitude,
            centerLongitude = centerLongitude
        )

        val maxSize = style.getLayerAs<RasterLayer>(
            MAIN_LAYER_ID
        )?.maxZoom?.toDouble() ?: 18.0

        if (calculateZoomForBounds(
                northEast = bounds.northEast,
                southWest = bounds.southWest
            ) > maxSize
        ) {
            mapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng = LatLng(
                        latitude = centerLatitude,
                        longitude = centerLongitude
                    ),
                    zoom = maxSize
                )
            )
        } else {
            mapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds = bounds,
                    paddingRight = ANIMATE_TO_RADIUS_BOUNDS_PADDING,
                    paddingLeft = ANIMATE_TO_RADIUS_BOUNDS_PADDING,
                    paddingBottom = 0,
                    paddingTop = 0
                )
            )
        }
    }

    private fun updateVesselDistanceFromAnchor(distance: Double) {
        binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
            distanceFromAnchorLinearLayout.visibility = View.VISIBLE
            distanceFromAnchorTextView.text =
                getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                    round(distance * 10) / 10,
                    "M"
                )
        }
    }

    private fun showAnchorageAlarmBottomSheet(
        style: Style,
        mapLibreMap: MapLibreMap,
        latLng: LatLng,
        // if null, current anchorage is not saved in anchorage's history and should be cached
        anchorageHistoryUiModel: AnchorageHistoryUiModel? = null
    ) {
        shouldInterceptBackEvent = true

        anchorageAlarmBottomSheetLayout.visibility = View.VISIBLE
        anchorageAlarmBottomSheetBehavior.state = STATE_EXPANDED

        if (AnchorageAlarmService.runningAnchorageAlarm.value.isRunning) {
            navigateToAnchorageRadius(
                style = style,
                mapLibreMap = mapLibreMap,
                radiusInMeters = AnchorageAlarmService.runningAnchorageAlarm.value.radius,
                centerLatitude = latLng.latitude,
                centerLongitude = latLng.longitude
            )

            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
                radiusValueTextView.text =
                    getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                        AnchorageAlarmService.runningAnchorageAlarm.value.radius.toInt().toString(),
                        "M"
                    )

                radiusSlider.apply {
                    value = AnchorageAlarmService.runningAnchorageAlarm.value.radius
                    addOnChangeListener { _, value, _ ->
                        radiusValueTextView.text =
                            getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                                value.toInt().toString(),
                                "M"
                            )

                        updateAnchorageRadius(style = mapStyle, latitude = latLng.latitude)
                    }
                }
            }

            AnchorageAlarmService.runningAnchorageAlarm.value.takeIf { it.isRunning }?.run {
                mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let { latLng ->

                    updateVesselDistanceFromAnchor(
                        latLng.distanceTo(LatLng(latitude, longitude))
                    )
                }
            }
        } else {
            viewModel.setAnchorageCenterPoint(latLng) // save center point only if there is no running service

            binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
                radiusValueTextView.text =
                    getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                        radiusSlider.value.toInt().toString(),
                        "M"
                    )

                radiusSlider.apply {
                    addOnChangeListener { _, value, _ ->
                        radiusValueTextView.text =
                            getString(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                                value.toInt().toString(),
                                "M"
                            )

                        updateAnchorageRadius(
                            style = mapStyle,
                            latitude = viewModel.anchorageCenterPoint.value!!.latitude
                        )

                        // update camera based on new rendered radius
                        navigateToAnchorageRadius(
                            style = style,
                            mapLibreMap = mapLibreMap,
                            radiusInMeters = value,
                            centerLatitude = viewModel.anchorageCenterPoint.value!!.latitude,
                            centerLongitude = viewModel.anchorageCenterPoint.value!!.longitude
                        )
                    }
                }

                // anchorage center point moved, reset the calculated distance
                distanceFromAnchorTextView.text = null
                distanceFromAnchorLinearLayout.visibility = View.GONE

                navigateToAnchorageRadius(
                    style = style,
                    mapLibreMap = mapLibreMap,
                    radiusInMeters = radiusSlider.value,
                    centerLatitude = viewModel.anchorageCenterPoint.value!!.latitude,
                    centerLongitude = viewModel.anchorageCenterPoint.value!!.longitude
                )
            }
        }

        binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetMainContentId.apply {
            startButton.setOnClickListener {
                if (isLocationPermissionGranted) {
                    // Start service
                    Intent(activity, AnchorageAlarmService::class.java).also {
                        it.setAction(AnchorageAlarmService.Actions.START.toString())
                        it.putExtra(AnchorageAlarmService.EXTRA_RADIUS, radiusSlider.value)
                        it.putExtra(
                            AnchorageAlarmService.EXTRA_LATITUDE,
                            viewModel.anchorageCenterPoint.value!!.latitude
                        )
                        it.putExtra(
                            AnchorageAlarmService.EXTRA_LONGITUDE,
                            viewModel.anchorageCenterPoint.value!!.longitude
                        )
                        requireActivity().startService(it)
                    }

                    // id and timestamp are omitted from equals and hashCode methods, so only latitude,
                    // longitude and radius are checked for equality
                    if (anchorageHistoryUiModel != AnchorageHistoryUiModel(
                            id = "",
                            latitude = viewModel.anchorageCenterPoint.value!!.latitude,
                            longitude = viewModel.anchorageCenterPoint.value!!.longitude,
                            radius = radiusSlider.value.toInt(),
                            timestamp = 0L
                        )
                    ) {
                        // save current anchorage location to persistent proto datastore
                        viewModel.saveAnchorageToHistory(
                            AnchorageHistoryUiModel(
                                id = UUID.randomUUID().toString(),
                                latitude = viewModel.anchorageCenterPoint.value!!.latitude,
                                longitude = viewModel.anchorageCenterPoint.value!!.longitude,
                                radius = radiusSlider.value.toInt(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        viewModel.updateAnchorageHistoryTimestamp(
                            id = anchorageHistoryUiModel.id,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                } else activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            stopButton.setOnClickListener {
                // Stop service
                Intent(
                    activity,
                    AnchorageAlarmService::class.java
                ).also {
                    it.setAction(AnchorageAlarmService.Actions.STOP.toString())
                    requireActivity().startService(it)
                }
            }

            closeButton.setOnClickListener {
                mapSharedViewModel.setShowAnchorageAlarmBottomSheet(false)
            }

            moveImageButton.setOnClickListener {
                viewModel.setIsAnchorageRepositionEnabled(true)
            }

            settingsImageButton.setOnClickListener {
                findNavController().navigate(R.id.action_mapFragment_to_anchorageAlarmSettingsDialog)
            }

            showAnchorageRadius(
                style = mapStyle,
                radiusInPixels = radiusInMetersToRadiusInPixels(
                    mapLibreMap = mapLibre,
                    radiusInMeters = radiusSlider.value,
                    latitude = viewModel.anchorageCenterPoint.value!!.latitude
                ),
                latLng = viewModel.anchorageCenterPoint.value!!
            )
        }
    }

    // Takes MarkerInfoModel state as an argument and renders it.
    private fun showMarkerBottomSheet(markerInfo: MarkerInfoModel) {
        if (markerBottomSheetBehavior.state == STATE_EXPANDED) closeMarkerBottomSheetLayout()

        shouldInterceptBackEvent = true
        openMarkerBottomSheetLayout()

        adjustMapViewPositionIfNeeded(
            bottomSheetLayout = markerBottomSheetLayout,
            point = markerInfo.position
        )

        markerInfo.run {
            binding.markerBottomSheetId.apply {
                // set title
                markerTitle.apply {
                    text = title
                }

                // load prop images (e.g.: Wi-Fi, wheelchair)
                loadMarkerBottomSheetPropImages(markerInfo.propImages)

                // set type
                type?.let {
                    markerType.apply {
                        visibility = View.VISIBLE
                        text = type
                    }
                } ?: run {
                    markerType.visibility = View.GONE
                }

                // set description
                description?.let { description ->
                    descriptionLinearLayout.visibility = View.VISIBLE
                    markerBottomSheetDescriptionTextView.text = description
                } ?: run {
                    descriptionLinearLayout.visibility = View.GONE
                }

                // set contact information
                contacts?.let { contacts ->
                    contactsLinearLayout.visibility = View.VISIBLE
                    contactsContainer.text = contacts
                } ?: run {
                    contactsLinearLayout.visibility = View.GONE
                }

                // set symbol icon drawable
                iconImage?.let { image ->
                    markerIconLinearLayout.visibility = View.VISIBLE
                    markerBottomSheetImageView.setImageDrawable(image)
                } ?: run {
                    markerIconLinearLayout.visibility = View.GONE
                }

                // load image if present
                image?.let { url ->
                    imageViewCardViewContainer.visibility = View.VISIBLE
                    imageView.load(url) {
                        crossfade(true)
                        crossfade(300)
                    }
                } ?: run {
                    imageView.setImageDrawable(null)
                    imageViewCardViewContainer.visibility = View.GONE
                }

                // set geolocation information
                markerBottomSheetGeopointTextView.text =
                    resources.getString(com.bytecause.core.resources.R.string.split_two_strings_formatter)
                        .format(
                            MapUtil.latitudeToDMS(position.latitude),
                            MapUtil.longitudeToDMS(position.longitude),
                        )

                mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                    ?.let { latLng ->
                        markerBottomSheetDistanceTextView.apply {
                            if (visibility != View.VISIBLE) visibility = View.VISIBLE
                            text =
                                resources.getString(com.bytecause.core.resources.R.string.distance_from_geopoint)
                                    .format(
                                        com.bytecause.util.string.StringUtil.formatDistanceDoubleToString(
                                            position.distanceTo(
                                                latLng,
                                            ),
                                        ),
                                    )
                        }
                        markerBottomSheetBearingTextView.apply {
                            if (visibility != View.VISIBLE) visibility = View.VISIBLE
                            text =
                                resources.getString(com.bytecause.core.resources.R.string.bearing_from_geopoint)
                                    .format(
                                        com.bytecause.util.string.StringUtil.formatBearingDegrees(
                                            latLng.bearingTo(position),
                                        ),
                                    )
                        }
                    } ?: run {
                    markerBottomSheetDistanceTextView.visibility = View.GONE
                    markerBottomSheetBearingTextView.visibility = View.GONE
                }
            }
        }
    }

    private fun updateAnchorageRadiusCenterPositionTextView(latLng: LatLng) {
        binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetRepositionLayout.anchorageRadiusCenterPositionTextView.text =
            getString(
                com.bytecause.core.resources.R.string.split_two_strings_formatter,
                String.format(Locale.getDefault(), "%.5f", latLng.latitude),
                String.format(Locale.getDefault(), "%.5f", latLng.longitude)
            )
    }

    private fun loadMarkerBottomSheetPropImages(images: List<Int>) {
        val container = binding.markerBottomSheetId.imageContainer.apply {
            visibility = if (images.isNotEmpty()) View.VISIBLE else View.GONE
        }
        // Clear any existing views
        container.removeAllViews()

        // Iterate over the drawable resource IDs and add ImageViews
        for (drawableId in images) {
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    50,
                    50
                )
                val drawable = ContextCompat.getDrawable(requireContext(), drawableId)?.apply {
                    setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            com.bytecause.core.resources.R.color.md_theme_onSurface
                        )
                    )
                }
                setImageDrawable(drawable)
            }

            val spacer = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    20,
                    20
                )
            }

            container.apply {
                addView(imageView)
                addView(spacer)
            }
        }
    }

    private fun loadMapStyle(
        map: MapLibreMap,
        onStyleLoaded: (Style) -> Unit
    ) {
        map.setStyle(
            Style.Builder().fromUri("asset://style.json")
        ) { style ->
            onStyleLoaded(style)

            // if symbolManager is not null and new style is loaded, we need to re-assign new instance
            // of this newly loaded style, because previous style's instance is no longer valid.
            symbolManager?.let {
                // Additional check for symbol manager's layer in style
                if (style.getLayer(it.layerId) == null) {
                    symbolManager = SymbolManager(mapView!!, map, style)
                }
            }
        }
    }


    // Saves camera state, so the last position is preserved across device's configuration changes.
    private fun saveMapState() {
        if (!::mapLibre.isInitialized) return
        mapSharedViewModel.saveCameraPosition(mapLibre.cameraPosition)
    }

    // Checks if camera is centered on the map.
    private fun isCentered(): Boolean {
        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let { geoPoint ->
            val inCenter: Boolean
            val cameraCenterInScreenPixels =
                mapLibre.projection.toScreenLocation(
                    mapLibre.cameraPosition.target ?: LatLng(
                        0.0,
                        0.0
                    )
                )
            val currentLocationInScreenPixels =
                mapLibre.projection.toScreenLocation(geoPoint)
            val threshold = 10

            inCenter =
                abs(cameraCenterInScreenPixels.x - currentLocationInScreenPixels.x) <= threshold &&
                        abs(cameraCenterInScreenPixels.y - currentLocationInScreenPixels.y) <= threshold

            return inCenter
        }
        return false
    }

    private fun setDisabledLocationDrawable() {
        if (!isLocationPermissionGranted) {
            binding.root.findViewById<ImageButton>(R.id.location_button)
                .setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        com.bytecause.core.resources.R.drawable.baseline_location_disabled_24,
                    ),
                )
        }
    }

    private fun setEnabledLocationDrawable() {
        if (isLocationPermissionGranted) {
            val locationButton =
                binding.root.findViewById<ImageButton>(R.id.location_button).apply {
                    tag = locationButtonState
                }

            when (locationButton.tag) {
                DEFAULT_BUTTON_STATE -> {
                    locationButton
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.baseline_location_searching_24,
                            ),
                        )
                    setDefaultCameraMode()
                }

                TRACKING_BUTTON_STATE -> {
                    locationButton
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.baseline_my_location_24,
                            ),
                        )
                    setCameraCompassTrackingMode()
                }
            }
        }
    }

    // This camera mode will rotate the map based on the device's direction and follow the user.
    private fun setCameraCompassTrackingMode() {
        if (locationComponent?.isLocationComponentEnabled == false) return
        locationComponent?.cameraMode = CameraMode.TRACKING_COMPASS
    }


    private fun setDefaultCameraMode() {
        if (locationComponent?.isLocationComponentEnabled == false) return
        locationComponent?.cameraMode = CameraMode.NONE
    }

    // Shows necessary layouts needed for distance measuring.
    private fun enterMeasureDistanceMode() {
        showMeasureBottomSheet()

        if (viewModel.isMeasuring) {
            binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility =
                View.VISIBLE
            showTargetOverlay()
            return
        }
        viewModel.setIsMeasuring(true)

        binding.mapTopRightPanelLinearLayout.layersButton.visibility = View.GONE
        binding.mapTopLeftPanelLinearLayout.leftLinearLayout.visibility = View.GONE

        val layoutParams =
            binding.mapTopRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 65

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.VISIBLE

        showTargetOverlay()
        mapSharedViewModel.latLngFlow.value?.let {
            val point = (it as PointType.Marker).latLng

            viewModel.addMeasurePoint(point)
            mapLibre.animateCamera(CameraUpdateFactory.newLatLng((point)))
            mapSharedViewModel.setLatLng(null)
        }
    }

    // Hides layouts that are no longer needed after distance measure is done.
    private fun leaveMeasureDistanceMode() {
        viewModel.setIsMeasuring(false)
        mapSharedViewModel.latLngFlow.value?.let {
            mapLibre.animateCamera(CameraUpdateFactory.newLatLng((it as PointType.Marker).latLng))
        }

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.GONE
        binding.mapTopRightPanelLinearLayout.layersButton.visibility = View.VISIBLE
        binding.mapTopLeftPanelLinearLayout.leftLinearLayout.visibility = View.VISIBLE
        measureBottomSheetBehavior.state = STATE_HIDDEN
        showBottomSheetLayout()

        (binding.mapTopRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = 0
        }

        // Don't clear measure points list entirely, first point in list is needed for restoring state of
        // latLngFlow.
        viewModel.clearMeasurePoints()

        binding.measureDistanceBottomSheet.distanceTextview.text = null
        hideTargetOverlay()
    }

    private fun unregisterSensors() {
        sensorManager?.run {
            unregisterListener(sensorListener)
            sensorManager = null

            // Reset view state
            binding.anchorageAlarmBottomSheet
                .anchorageAlarmBottomSheetRepositionLayout
                .getBearingFromCompassImageButton
                .setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun resetAnchorageRepositionViewState() {
        binding.anchorageAlarmBottomSheet.anchorageAlarmBottomSheetRepositionLayout.apply {
            latitudeEditText.apply {
                text = null
                error = null
            }
            longitudeEditText.apply {
                text = null
                error = null
            }
            distanceEditText.apply {
                text = null
                error = null
            }
            bearingEditText.apply {
                text = null
                error = null
            }
        }
    }

    // Shows target in the center of mapView.
    private fun showTargetOverlay() {
        binding.targetOverlay.visibility = View.VISIBLE
    }

    private fun hideTargetOverlay() {
        binding.targetOverlay.visibility = View.GONE
    }

    private fun hideBottomSheetLayout() {
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun addMeasurePoint() {
        // Calculate the screen position of the center
        val center = PointF(mapView!!.width / 2f, mapView!!.height / 2f)
        // Calculate the geographical position of the center
        val centerGeoPoint = mapLibre.projection.fromScreenLocation(center)

        if (!MapUtil.arePointsWithinDelta(
                viewModel.measurePointsSharedFlow.replayCache.last().lastOrNull(),
                centerGeoPoint,
            )
        ) {
            viewModel.addMeasurePoint(centerGeoPoint)
        }
    }

    private fun removeSearchPlace() {
        // Removes place boundaries if rendered
        if (mapSharedViewModel.searchPlace.value != null) {
            if (boundaryManager != null) {
                boundaryManager?.deleteAll()
                boundaryManager = null
            }

            // Removes searched poi marker
            mapSharedViewModel.setSearchPlace(null)
            removeSearchBoxText(SearchBoxTextType.PoiName())
        }
    }

    private fun removeMarker() {
        // Removes marker placed by the user
        mapSharedViewModel.setLatLng(null)
        removeSearchBoxText(SearchBoxTextType.Coordinates())
    }

    private fun closeBottomSheetLayout() {
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun showBottomSheetLayout() {
        bottomSheetLayout.visibility = View.VISIBLE
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun showMeasureBottomSheet() {
        measureBottomSheetLayout.visibility = View.VISIBLE
        measureBottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun closeMarkerBottomSheetLayout() {
        markerBottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun openMarkerBottomSheetLayout() {
        markerBottomSheetLayout.visibility = View.VISIBLE
        markerBottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun openBottomSheetLayout(
        p: LatLng,
        text: String? = null,
    ) {
        shouldInterceptBackEvent = true

        if (bottomSheetLayout.visibility == View.GONE) {
            binding.bottomSheetId.textPlaceHolder.apply {
                this.text = text ?: getString(
                    com.bytecause.core.resources.R.string.split_two_strings_formatter,
                    MapUtil.latitudeToDMS(p.latitude),
                    MapUtil.longitudeToDMS(p.longitude),
                )
                setSearchBoxText(SearchBoxTextType.Coordinates(this.text.toString()))
            }
            showBottomSheetLayout()
        } else {
            binding.bottomSheetId.textPlaceHolder.apply {
                this.text = text ?: getString(
                    com.bytecause.core.resources.R.string.split_two_strings_formatter,
                    MapUtil.latitudeToDMS(p.latitude),
                    MapUtil.longitudeToDMS(p.longitude),
                )
                setSearchBoxText(SearchBoxTextType.Coordinates(this.text.toString()))
            }
        }

        adjustMapViewPositionIfNeeded(
            bottomSheetLayout = bottomSheetLayout,
            point = p
        )
    }

    private fun setSearchBoxText(text: SearchBoxTextType) {
        viewModel.insertTextIntoSearchBoxTextPlaceholder(text)
    }

    private fun removeSearchBoxText(text: SearchBoxTextType) {
        viewModel.removeTextFromSearchBoxTextPlaceholder(text)
    }

    @SuppressLint("MissingPermission")
    private fun activateLocationComponent(style: Style) {
        if (requireContext().isLocationPermissionGranted()) {
            locationComponent = mapLibre.locationComponent

            val locationComponentOptions =
                LocationComponentOptions.builder(requireContext())
                    .pulseEnabled(true)
                    .pulseMaxRadius(20f)
                    .accuracyAnimationEnabled(true)
                    .compassAnimationEnabled(true)
                    .build()

            locationComponent?.activateLocationComponent(
                LocationComponentActivationOptions
                    .builder(requireContext(), style)
                    .locationComponentOptions(locationComponentOptions)
                    .build(),
            )
            locationComponent?.apply {
                isLocationComponentEnabled = true
                renderMode = RenderMode.COMPASS

                val request = LocationEngineRequest.Builder(5000)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(2000)
                    .build()

                setEnabledLocationDrawable()

                val locationLooper = Looper.getMainLooper()
                locationEngine?.requestLocationUpdates(
                    request,
                    locationEngineCallback,
                    locationLooper,
                )
            }
        }
    }

    private fun animateCameraToLocation() {
        locationComponent?.lastKnownLocation?.run {
            mapLibre.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude,
                        longitude,
                    ),
                    ZOOM_IN_DEFAULT_LEVEL,
                ),
            )
        }
    }

    private fun cancelAnimation() {
        circleLayerAnimator?.cancel()
        circleLayerAnimator = null
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        locationComponent?.onStart()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationComponent?.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        locationComponent?.onStop()
    }

    override fun onResume() {
        super.onResume()

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        markerBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        measureBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        anchorageAlarmBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        sensorManager?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        sensorManager?.run {
            registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        markerBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        measureBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        anchorageAlarmBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)

        sensorManager?.unregisterListener(sensorListener)

        cancelAnimation()

        saveMapState()

        mapView?.onPause()
    }
}
