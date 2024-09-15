package com.bytecause.map.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
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
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.domain.util.PoiTagsUtil.excludeDescriptionKeysFromTags
import com.bytecause.domain.util.PoiTagsUtil.extractContactsFromTags
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.domain.util.PoiTagsUtil.getPoiType
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.FragmentMapBinding
import com.bytecause.map.ui.model.MarkerInfoModel
import com.bytecause.map.ui.viewmodel.MapViewModel
import com.bytecause.map.util.MapFragmentConstants.ANIMATED_CIRCLE_COLOR
import com.bytecause.map.util.MapFragmentConstants.ANIMATED_CIRCLE_RADIUS
import com.bytecause.map.util.MapFragmentConstants.COMPASS_BOTTOM_MARGIN
import com.bytecause.map.util.MapFragmentConstants.COMPASS_LEFT_MARGIN
import com.bytecause.map.util.MapFragmentConstants.COMPASS_RIGHT_MARGIN
import com.bytecause.map.util.MapFragmentConstants.COMPASS_TOP_MARGIN
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
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_DEFAULT_SIZE
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_LAYER
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_PROPERTY_ID_KEY
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY
import com.bytecause.map.util.MapFragmentConstants.HARBOUR_SYMBOL_SELECTED_SIZE
import com.bytecause.map.util.MapFragmentConstants.LINE_WIDTH
import com.bytecause.map.util.MapFragmentConstants.MAP_MARKER
import com.bytecause.map.util.MapFragmentConstants.PIN_ICON
import com.bytecause.map.util.MapFragmentConstants.POIS_VISIBILITY_ZOOM_LEVEL
import com.bytecause.map.util.MapFragmentConstants.POI_GEOJSON_SOURCE
import com.bytecause.map.util.MapFragmentConstants.POI_CATEGORY_KEY
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
import com.bytecause.map.util.MapUtil.Companion.drawLine
import com.bytecause.map.util.navigateToCustomPoiNavigation
import com.bytecause.map.util.navigateToFirstRunNavigation
import com.bytecause.map.util.navigateToSearchNavigation
import com.bytecause.presentation.components.views.CustomTextInputEditText
import com.bytecause.presentation.interfaces.DrawerController
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.common.LastClick
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.map.MapUtil.Companion.bearingTo
import com.bytecause.util.map.TileSourceLoader
import com.bytecause.util.poi.PoiUtil
import com.bytecause.util.poi.PoiUtil.createLayerDrawable
import com.bytecause.util.poi.PoiUtil.extractPropImagesFromTags
import com.bytecause.util.poi.PoiUtil.poiIconDrawableMap
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
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconRotate
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
    private var lineManager: LineManager? = null
    private var boundaryManager: LineManager? = null
    private var markerSymbol: Symbol? = null
    private var vesselsFeatureCollection: FeatureCollection? = null
    private var harboursFeatureCollection: FeatureCollection? = null
    private var customPoiFeatureCollection: FeatureCollection? = null
    private var poisFeatureCollection: FeatureCollection? = null

    private var circleLayerAnimator: Animator? = null

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetCallback: BottomSheetCallback

    private lateinit var markerBottomSheetLayout: LinearLayout
    private lateinit var markerBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var measureBottomSheetLayout: LinearLayout
    private lateinit var measureBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

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
                    // Handle state changes (e.g., expanded, collapsed, hidden)
                    when (newState) {
                        STATE_HIDDEN -> {
                            when (bottomSheet.id) {
                                bottomSheetLayout.id -> {
                                    if (mapSharedViewModel.geoIntentFlow.replayCache.lastOrNull() != null) {
                                        mapSharedViewModel.resetGeoIntentFlow()
                                    }

                                    // Removes marker placed by the user
                                    mapSharedViewModel.setLatLng(null)
                                    setSearchBoxText(null)

                                    // Removes place boundaries if rendered
                                    if (mapSharedViewModel.placeToFindStateFlow.value != null) {
                                        if (boundaryManager != null) {
                                            boundaryManager?.deleteAll()
                                            boundaryManager = null
                                        }

                                        // Removes searched poi marker
                                        mapSharedViewModel.setPlaceToFind(null)
                                    }

                                    // Removes all poi markers of given category if rendered
                                    if (mapSharedViewModel.showPoiStateFlow.value != null) {
                                        mapSharedViewModel.setPoiToShow(
                                            null,
                                        )
                                    }
                                }

                                markerBottomSheetLayout.id -> {
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
                                    }
                                }

                                else -> return
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
                maxHeight = Resources.getSystem().displayMetrics.heightPixels / 2
                isHideable = true
                state = STATE_HIDDEN
            }

        markerBottomSheetLayout = binding.markerBottomSheetId.markerBottomSheetLayout
        markerBottomSheetBehavior =
            BottomSheetBehavior.from(markerBottomSheetLayout).apply {
                maxHeight = Resources.getSystem().displayMetrics.heightPixels / 2
                isHideable = true
                state = STATE_HIDDEN
            }

        measureBottomSheetLayout = binding.measureDistanceBottomSheet.measureDistanceBottomSheet
        measureBottomSheetBehavior =
            BottomSheetBehavior.from(measureBottomSheetLayout).apply {
                maxHeight = Resources.getSystem().displayMetrics.heightPixels / 2
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

                            activateLocationComponent(style)

                            // Init marker icons
                            mapStyle.addImages(
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

                                        if (!viewModel.isMeasuring) {
                                            if (lineManager != null) {
                                                lineManager?.deleteAll()
                                                symbolManager?.deleteAll()
                                                mapSharedViewModel.setLatLng(points.first())
                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLng(
                                                        points.first(),
                                                    ),
                                                )
                                                lineManager = null
                                                // latLngFlow state restored, now we can clear entire list of
                                                // measure points.
                                                viewModel.clearMeasurePoints(entireClear = true)
                                            }
                                            return@collect
                                        }

                                        if (binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility == View.GONE) {
                                            enterMeasureDistanceMode()
                                        }

                                        if (lineManager == null) {
                                            lineManager =
                                                LineManager(mapView!!, mapLibreMap, mapStyle)
                                        }

                                        // Clear any existing polylines if necessary
                                        lineManager?.deleteAll()

                                        // Draw polyline
                                        drawLine(
                                            polylineList = points,
                                            lineManager = lineManager,
                                            lineColor = requireContext().getColor(com.bytecause.core.resources.R.color.black),
                                            lineWidth = LINE_WIDTH,
                                        )

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
                                                val distanceTextViewText =
                                                    if (distance > 1000) {
                                                        ((round(distance / 1000 * 10) / 10).toString()) + " KM"
                                                    } else {
                                                        round(
                                                            distance,
                                                        ).toInt().toString() + " M"
                                                    }

                                                binding.measureDistanceBottomSheet.distanceTextview.text =
                                                    distanceTextViewText
                                            }
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

                                            mapView?.invalidate()

                                            return@collect
                                        }
                                        if (viewModel.isMeasuring) return@collect

                                        if (symbolManager == null) {
                                            symbolManager =
                                                SymbolManager(
                                                    mapView!!,
                                                    mapLibreMap,
                                                    mapStyle
                                                )
                                        }

                                        addMarker(point)
                                    }
                                }
                            }

                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.placeToFindStateFlow.collect { place ->
                                        place ?: return@collect

                                        boundaryManager?.deleteAll()

                                        val latLng = LatLng(place.latitude, place.longitude)

                                        place.polygonCoordinates.takeIf { coordinates -> coordinates.isNotEmpty() }
                                            ?.let { encodedPolygon ->
                                                com.bytecause.util.algorithms.PolylineAlgorithms()
                                                    .decode(encodedPolygon)
                                                    .let { polylineList ->

                                                        boundaryManager =
                                                            LineManager(
                                                                mapView!!,
                                                                mapLibreMap,
                                                                mapStyle
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
                                                                    },
                                                                ),
                                                                padding = 50,
                                                            ),
                                                        )
                                                    }
                                            } ?: run {
                                            mapLibreMap.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    latLng,
                                                    13.0,
                                                ),
                                            )
                                        }

                                        mapSharedViewModel.setLatLng(latLng)
                                        setSearchBoxText(place.name)
                                    }
                                }
                            }

                            // Shows poi on map of the given category (e.g.: cinema, cafe, ...)
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    mapSharedViewModel.showPoiStateFlow.collect { poiMap ->

                                        mapStyle.apply {
                                            if (getSourceAs<GeoJsonSource>(
                                                    POI_GEOJSON_SOURCE
                                                ) != null
                                            ) {
                                                removeLayer(POI_SYMBOL_LAYER)
                                                removeSource(POI_GEOJSON_SOURCE)
                                                setSearchBoxText(null)
                                            }
                                        }

                                        if (poiMap.isNullOrEmpty()) return@collect

                                        // poiMap holds category name and List of IDs which are used for search in database.
                                        viewModel.searchInCache(poiMap.values.flatten())
                                            .let { flow ->
                                                flow.firstOrNull()?.let { poiEntityList ->

                                                    // Cache drawable resources
                                                    val drawableCache =
                                                        mutableMapOf<String, Drawable>()

                                                    setSearchBoxText(
                                                        poiMap.keys.toList().firstOrNull()
                                                    )

                                                    val points = mutableListOf<LatLng>()
                                                    val features = mutableListOf<Feature>()

                                                    for (poi in poiEntityList) {
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
                                                                drawable = poiIconDrawableMap[poi.category]?.let {
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

                                                    mapStyle.apply {
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
                                                            50,
                                                        ),
                                                    )
                                                }
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
                                            mapStyle.apply {
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
                                                    drawable = poiIconDrawableMap[PoiUtil.unifyPoiDrawables(poi.category)]?.let {
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

                                        mapStyle.getSourceAs<GeoJsonSource>(POI_GEOJSON_SOURCE)
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

                                            mapStyle.apply {
                                                // iterate over drawable map entries and add it's values into
                                                // maplibre's style
                                                drawableCache.entries.forEach { entry ->
                                                    addImage(entry.key, entry.value)
                                                }
                                                addSource(geoJsonSource)
                                                addLayer(symbolLayer)
                                            }
                                            return@collect
                                        }

                                        mapStyle.apply {
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

                                        mapStyle.getSourceAs<GeoJsonSource>(VESSEL_GEOJSON_SOURCE)
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

                                            mapStyle.apply {
                                                drawableCache.entries.forEach { entry ->
                                                    addImage(entry.key, entry.value)
                                                }

                                                addSource(geoJsonSource)
                                                addLayer(unclusteredLayer)
                                            }
                                            return@collect
                                        }

                                        mapStyle.apply {
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

                                        mapStyle.getSourceAs<GeoJsonSource>(HARBOUR_GEOJSON_SOURCE)
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
                                                        iconImage("harbour_icon"),
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

                                            mapStyle.apply {
                                                ContextCompat.getDrawable(
                                                    requireContext(),
                                                    com.bytecause.core.resources.R.drawable.harbour_marker_icon
                                                )?.let {
                                                    addImage(
                                                        "harbour_icon",
                                                        it
                                                    )
                                                }

                                                addSource(geoJsonSource)
                                                addLayer(unclusteredLayer)
                                            }
                                            return@collect
                                        }

                                        mapStyle.apply {
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                com.bytecause.core.resources.R.drawable.harbour_marker_icon
                                            )?.let {
                                                addImage(
                                                    "harbour_icon",
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
                                            mapStyle.apply {
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

                                        mapStyle.getSourceAs<GeoJsonSource>(
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

                                                mapStyle.apply {
                                                    drawableCache.entries.forEach { entry ->
                                                        addImage(entry.key, entry.value)
                                                    }
                                                    addSource(geoJsonSource)
                                                    addLayer(symbolLayer)
                                                }
                                                return@collect
                                            }

                                        mapStyle.apply {
                                            drawableCache.entries.forEach { entry ->
                                                addImage(entry.key, entry.value)
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
                                            if (mapStyle.getSourceAs<GeoJsonSource>(
                                                    VESSEL_GEOJSON_SOURCE
                                                ) == null
                                            ) return@collect

                                            // Remove vessels layer and source
                                            mapStyle.apply {
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
                                        if (!::mapLibre.isInitialized) return@collect

                                        if (isVisible) {
                                            viewModel.updateHarbourBbox(mapLibre.projection.visibleRegion.latLngBounds)
                                        } else {
                                            if (mapStyle.getSourceAs<GeoJsonSource>(
                                                    HARBOUR_GEOJSON_SOURCE
                                                ) == null
                                            ) return@collect

                                            // Remove harbours layer and source
                                            mapStyle.apply {
                                                removeLayer(HARBOUR_SYMBOL_LAYER)
                                                removeSource(HARBOUR_GEOJSON_SOURCE)
                                                removePulsingCircleLayer(this)

                                                harboursFeatureCollection = null
                                            }
                                        }
                                    }
                                }
                            }

                            // Update feature selected state (feature = rendered symbol marker)
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    viewModel.selectedFeatureIdFlow.collect { featureType ->

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
                                                    mapStyle.getSourceAs<GeoJsonSource>(
                                                        VESSEL_GEOJSON_SOURCE
                                                    )
                                                        ?.setGeoJson(
                                                            vesselsFeatureCollection
                                                        )
                                                    removePulsingCircleLayer(mapStyle)
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

                                                            mapStyle.getSourceAs<GeoJsonSource>(
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
                                                    mapStyle.getSourceAs<GeoJsonSource>(
                                                        CUSTOM_POI_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        customPoiFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(mapStyle)
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

                                                            mapStyle.getSourceAs<GeoJsonSource>(
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
                                                    mapStyle.getSourceAs<GeoJsonSource>(
                                                        HARBOUR_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        harboursFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(mapStyle)
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

                                                            mapStyle.getSourceAs<GeoJsonSource>(
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
                                                    mapStyle.getSourceAs<GeoJsonSource>(
                                                        POI_GEOJSON_SOURCE
                                                    )?.setGeoJson(
                                                        poisFeatureCollection
                                                    )

                                                    removePulsingCircleLayer(mapStyle)
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

                                                            mapStyle.getSourceAs<GeoJsonSource>(
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
                                            mapSharedViewModel.setLatLng(latLng)
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
                        }

                        addOnMoveListener(
                            object : OnMoveListener {
                                override fun onMove(detector: MoveGestureDetector) {
                                    if (!locationComponent.isLocationComponentActivated) return
                                    if (locationComponent.cameraMode == DEFAULT_BUTTON_STATE) return
                                    viewModel.setLocationButtonState(DEFAULT_BUTTON_STATE)
                                }

                                override fun onMoveBegin(detector: MoveGestureDetector) {}
                                override fun onMoveEnd(detector: MoveGestureDetector) {}
                            },
                        )

                        addOnMapLongClickListener {
                            mapSharedViewModel.setLatLng(it)
                            true
                        }

                        if (mapSharedViewModel.geoIntentFlow.replayCache.lastOrNull() == null) {
                            viewLifecycleOwner.lifecycleScope.launch {
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
                        uiSettings.apply {
                            compassGravity = Gravity.START
                            setCompassMargins(
                                COMPASS_LEFT_MARGIN,
                                COMPASS_TOP_MARGIN,
                                COMPASS_RIGHT_MARGIN,
                                COMPASS_BOTTOM_MARGIN,
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

        binding.mapRightPanelLinearLayout.locationButton.setOnClickListener {
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

        binding.mapRightPanelLinearLayout.layersButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_mapFragment_to_mapBottomSheetFragment)
        }

        binding.mapLeftPanelLinearLayout.customizeMapImageButton.setOnClickListener {
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

            mapSharedViewModel.latLngFlow.value?.let { latLng ->
                val action =
                    MapFragmentDirections.actionMapFragmentToMapShareBottomSheetDialog(
                        floatArrayOf(
                            latLng.latitude.toFloat(),
                            latLng.longitude.toFloat(),
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
                            binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility =
                                View.GONE
                            binding.mapRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
                                View.GONE
                        }

                        false -> {
                            binding.searchMapBox.searchBoxLinearLayout.visibility =
                                View.VISIBLE
                            binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility =
                                View.VISIBLE
                            binding.mapRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
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

    // Adds a layer on which to render the tapped animation.
    private fun addPulsingCircleLayer(style: Style) {
        val circleSource = GeoJsonSource(PULSING_CIRCLE_GEOJSON_SOURCE)
        style.addSource(circleSource)

        val circleLayer =
            CircleLayer(PULSING_CIRCLE_LAYER, PULSING_CIRCLE_GEOJSON_SOURCE)
                .withProperties(
                    PropertyFactory.circleColor(ANIMATED_CIRCLE_COLOR)
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
                        PropertyFactory.circleRadius(value),
                        PropertyFactory.circleOpacity(1f - (value / ANIMATED_CIRCLE_RADIUS)),
                    )
                } else {
                    // Value is greater than 14.5f, circle is shrinking, hide circle to prevent transition glitch
                    circleLayer?.setProperties(
                        PropertyFactory.circleRadius(0f),
                        PropertyFactory.circleOpacity(0f),
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
                        description = excludeDescriptionKeysFromTags(poi.tags).takeIf { it.isNotBlank() }/*poi.tags.entries.joinToString(
                            separator = "\n"
                        ) { (key, value) ->
                            "${formatTagString(key)}: ${formatTagString(value)}"
                        }*/,
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
                        description = harbour.tags.toList().joinToString("\n"),
                        image = replaceHttpWithHttps(harbour.tags["image"]),
                        position = LatLng(harbour.latitude, harbour.longitude)
                    )
                )
            }
        }
    }

    // when POI marker is hidden after bottom sheet expansion, camera will be adjusted.
    private fun adjustMapViewPositionIfNeeded(point: LatLng) {
        markerBottomSheetLayout.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    markerBottomSheetLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val markerBottomSheetHeight = markerBottomSheetLayout.height
                    val mapViewHeight = mapView!!.height

                    val projection = mapLibre.projection
                    val symbolScreenPoint = projection.toScreenLocation(point)
                    val range = Range.create(mapViewHeight - markerBottomSheetHeight, mapViewHeight)

                    if (range.contains(symbolScreenPoint.y.toInt())) {
                        val estimatedCenter = mapViewHeight - markerBottomSheetHeight

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


    // Takes MarkerInfoModel state as an argument and renders it.
    private fun showMarkerBottomSheet(markerInfo: MarkerInfoModel) {
        if (markerBottomSheetBehavior.state == STATE_EXPANDED) closeMarkerBottomSheetLayout()

        shouldInterceptBackEvent = true
        openMarkerBottomSheetLayout()

        adjustMapViewPositionIfNeeded(markerInfo.position)

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
                setImageResource(drawableId)
                setColorFilter(com.bytecause.core.resources.R.color.md_theme_onSurface)
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
        if (viewModel.isMeasuring) {
            binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility =
                View.VISIBLE
            showTargetOverlay()
            return
        }
        viewModel.setIsMeasuring(true)

        binding.mapRightPanelLinearLayout.layersButton.visibility = View.GONE
        binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility = View.GONE

        val layoutParams =
            binding.mapRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 65

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.VISIBLE
        measureBottomSheetBehavior.state = STATE_EXPANDED

        showTargetOverlay()
        mapSharedViewModel.latLngFlow.value?.let {
            viewModel.addMeasurePoint(it)
            mapLibre.animateCamera(CameraUpdateFactory.newLatLng(it))
            mapSharedViewModel.setLatLng(null)
        }
    }

    // Shows target in the center of mapView.
    private fun showTargetOverlay() {
        binding.targetOverlay.visibility = View.VISIBLE
    }

    private fun hideTargetOverlay() {
        binding.targetOverlay.visibility = View.GONE
    }

    // Hides layouts that are no longer needed after distance measure is done.
    private fun leaveMeasureDistanceMode() {
        viewModel.setIsMeasuring(false)
        mapSharedViewModel.latLngFlow.value?.let {
            mapLibre.animateCamera(CameraUpdateFactory.newLatLng(it))
        }

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.GONE
        binding.mapRightPanelLinearLayout.layersButton.visibility = View.VISIBLE
        binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility = View.VISIBLE
        measureBottomSheetBehavior.state = STATE_HIDDEN
        showBottomSheetLayout()

        (binding.mapRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = 0
        }

        // Don't clear measure points list entirely, first point in list is needed for restoring state of
        // latLngFlow.
        viewModel.clearMeasurePoints()

        binding.measureDistanceBottomSheet.distanceTextview.text = ""
        hideTargetOverlay()
    }

    private fun hideBottomSheetLayout() {
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    /**
     * Don't use directly!!!
     * To add marker on map, call setLatLng() function with LatLng argument inside MapSharedViewModel
     * */
    private fun addMarker(latLng: LatLng) {
        if (markerSymbol != null) {
            symbolManager?.delete(markerSymbol)
        }

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

        openBottomSheetLayout(latLng)
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

    private fun closeBottomSheetLayout() {
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun showBottomSheetLayout() {
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun closeMarkerBottomSheetLayout() {
        markerBottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun openMarkerBottomSheetLayout() {
        markerBottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun openBottomSheetLayout(
        p: LatLng,
        text: String? = null,
    ) {
        shouldInterceptBackEvent = true

        if (bottomSheetBehavior.state == STATE_HIDDEN) {
            binding.bottomSheetId.textPlaceHolder.apply {
                this.text = text ?: getString(
                    com.bytecause.core.resources.R.string.split_two_strings_formatter,
                    MapUtil.latitudeToDMS(p.latitude),
                    MapUtil.longitudeToDMS(p.longitude),
                )
                setSearchBoxText(this.text.toString())
            }
            showBottomSheetLayout()
        } else {
            binding.bottomSheetId.textPlaceHolder.apply {
                this.text = text ?: getString(
                    com.bytecause.core.resources.R.string.split_two_strings_formatter,
                    MapUtil.latitudeToDMS(p.latitude),
                    MapUtil.longitudeToDMS(p.longitude),
                )
                setSearchBoxText(this.text.toString())
            }
        }
    }

    private fun setSearchBoxText(text: String?) {
        binding.searchMapBox.searchMapEditText.setText(text)
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

                val request = LocationEngineRequest.Builder(10000)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(5000)
                    .build()

                setEnabledLocationDrawable()

                val locationLooper = Looper.getMainLooper()
                locationEngine?.requestLocationUpdates(
                    request,
                    object : LocationEngineCallback<LocationEngineResult> {
                        override fun onSuccess(result: LocationEngineResult?) {
                            if (result == null) return

                            result.lastLocation?.run {
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
                                    viewModel.saveUserLocation(it)
                                    mapSharedViewModel.setLastKnownPosition(it)
                                }
                                locationComponent?.forceLocationUpdate(this)
                            }
                        }

                        override fun onFailure(exception: Exception) {}
                    },
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

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        locationComponent?.onStart()
    }

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

        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        markerBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        measureBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        // cancel animation if not null
        circleLayerAnimator?.cancel()
        circleLayerAnimator = null

        saveMapState()

        mapView?.onPause()
    }
}
