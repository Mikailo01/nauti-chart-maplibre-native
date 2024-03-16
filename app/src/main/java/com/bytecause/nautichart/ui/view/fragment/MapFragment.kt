package com.bytecause.nautichart.ui.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.bytecause.nautichart.BuildConfig
import com.bytecause.nautichart.R
import com.bytecause.nautichart.animations.SmoothMapOrientationAnimator
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import com.bytecause.nautichart.databinding.FragmentMapBinding
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.interfaces.MapFragmentInterface
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.ui.util.isLocationPermissionGranted
import com.bytecause.nautichart.ui.view.activity.MainActivity
import com.bytecause.nautichart.ui.view.custom.CustomMapView
import com.bytecause.nautichart.ui.view.custom.CustomTextInputEditText
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.view.fragment.dialog.CustomizeMapDialogDirections
import com.bytecause.nautichart.ui.view.listeners.SensorListener
import com.bytecause.nautichart.ui.view.overlay.CustomCompassView
import com.bytecause.nautichart.ui.view.overlay.CustomMarker
import com.bytecause.nautichart.ui.view.overlay.CustomMyLocationNewOverlay
import com.bytecause.nautichart.ui.view.overlay.OverlayHelper
import com.bytecause.nautichart.ui.view.overlay.VesselRadiusMarkerClusterer
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.ui.viewmodels.MapViewModel
import com.bytecause.nautichart.util.MapUtil
import com.bytecause.nautichart.util.PolylineAlgorithms
import com.bytecause.nautichart.util.StringUtil
import com.bytecause.nautichart.util.TAG
import com.bytecause.nautichart.util.Util
import com.bytecause.nautichart.util.mapAsync
import com.bytecause.nautichart.util.swap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.GroundOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.properties.Delegates

fun <T> MutableList<Overlay>.addOverlay(
    overlay: T?,
    listener: MapFragmentInterface
) {
    when (overlay) {
        is Overlay -> {
            this.add(overlay)
            listener.overlayAddedListener()
        }

        is List<*> -> {
            if (overlay.all { it is Overlay }) {
                val overlayList = overlay as List<Overlay>
                this.addAll(overlayList)
                listener.overlayAddedListener()
            }
        }
    }
}

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), MapFragmentInterface {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private val viewModel: MapViewModel by activityViewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var mapView: CustomMapView

    private var isLocationPermissionGranted = false

    private lateinit var tileProvider: MapTileProviderBasic
    private lateinit var myLocationOverlay: CustomMyLocationNewOverlay
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var groundOverlay: GroundOverlay
    private lateinit var copyrightOverlay: CopyrightOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    private var highPriorityOverlays: List<Overlay> = listOf()

    private lateinit var overlayHelper: OverlayHelper

    private lateinit var mapEventsReceiver: MapEventsReceiver
    private lateinit var mapEventsOverlay: MapEventsOverlay

    private var mapRotationAnimation: SmoothMapOrientationAnimator? = null

    private val seamarks = CustomTileSourceFactory.OPEN_SEAMAP
    private var seamarksOverlay: TilesOverlay? = null

    private var gridOverlay: LatLonGridlineOverlay2? = null

    private lateinit var sensorListener: SensorListener

    private lateinit var slideAnimation: Animation

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetCallback: BottomSheetCallback

    private lateinit var markerBottomSheetLayout: LinearLayout
    private lateinit var markerBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var measureBottomSheetLayout: LinearLayout
    private lateinit var measureBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private var locationButtonState: Int = 0

    private var vesselsClusterer: VesselRadiusMarkerClusterer? = null

    private var vesselLoadingJob: Job? = null
    private var zoomAnimationJob: Job? = null
    private var searchForPoisJob: Job? = null

    private lateinit var customCompassView: CustomCompassView

    private var shouldInterceptBackEvent: Boolean by Delegates.observable(false) { _, _, newValue ->
        onBackPressedCallback.isEnabled = newValue
    }

    val util: Util = Util()

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                isLocationPermissionGranted = isGranted
                setEnabledLocationDrawable()
            } else {
                findNavController().navigate(R.id.action_map_dest_to_locationDialogFragment)
            }
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            when {
                // binding is initialized after onViewCreated, so navigating back to MapFragment from
                // another fragment will cause crash, so findViewById() was used to prevent from these crashes.
                view?.findViewById<LinearLayout>(R.id.measure_distance_bottom_sheet)?.isVisible == true -> leaveMeasureDistanceMode()
                view?.findViewById<LinearLayout>(R.id.bottomSheetLayout)?.isVisible == true -> bottomSheetBehavior.state =
                    STATE_HIDDEN

                view?.findViewById<LinearLayout>(R.id.marker_bottom_sheet_layout)?.isVisible == true -> closeMarkerBottomSheet()

                else -> {
                    if (isEnabled) Toast.makeText(
                        requireContext(),
                        "Press back again to exit.",
                        Toast.LENGTH_SHORT
                    ).show()
                    isEnabled = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Binding isn't available in onCreateView yet, so init only elements that don't require view at the time of their initialization.
        getInstance().load(
            requireContext().applicationContext,
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
        )
        getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // this checks if fragment went through configuration change or not, this will prevent from
        // showing another dialog after device's configuration changes.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (viewModel.getFirstRunFlag().firstOrNull() == null) {
                    findNavController().navigate(R.id.action_map_dest_to_firstRunDialogFragment)
                }
            }
        }

        windowInsetsController =
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            )

        mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (mapView.overlays.contains(overlayHelper.itemizedMarkerIconOverlay) && !overlayHelper.isMeasuring && bottomSheetLayout.isVisible) {
                    // Change bottom sheet state, which will invoke BottomSheetCallback()
                    bottomSheetBehavior.state = STATE_HIDDEN
                }
                if (binding.markerBottomSheetId.markerBottomSheetLayout.visibility == View.VISIBLE) closeMarkerBottomSheet()

                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (overlayHelper.isMeasuring && mapView.overlays.contains(overlayHelper.itemizedMarkerIconOverlay) || p == null) {
                    return false
                } else {
                    if (overlayHelper.geoPointList.isNotEmpty()) {
                        overlayHelper.clearGeoPointList()
                    }
                    mapView.overlays.remove(overlayHelper.itemizedMarkerIconOverlay)

                    if (binding.markerBottomSheetId.markerBottomSheetLayout.visibility == View.VISIBLE) closeMarkerBottomSheet()

                    overlayHelper.addMarkerToMapAndShowBottomSheet(p)
                }
                return true
            }
        }

        tileProvider = MapTileProviderBasic(context, seamarks)
        groundOverlay = GroundOverlay()
        mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        seamarksOverlay = TilesOverlay(tileProvider, context).apply {
            loadingLineColor = Color.TRANSPARENT
            loadingBackgroundColor = Color.TRANSPARENT
        }
        copyrightOverlay = CopyrightOverlay(requireContext())

        bottomSheetCallback = object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Handle state changes (e.g., expanded, collapsed, hidden)
                Log.d(TAG(), "newState")
                when (newState) {
                    STATE_HIDDEN -> {
                        when (bottomSheet.id) {
                            bottomSheetLayout.id -> {

                                mapView.overlays.remove(overlayHelper.itemizedMarkerIconOverlay)
                                mapView.overlays.removeAll(overlayHelper.boundaryLinePaths)
                                mapView.invalidate()

                                bottomSheetLayout.visibility = View.GONE
                                binding.searchMapBox.searchMapEditText.text = null
                                if (mapSharedViewModel.placeToFindStateFlow.value != null) mapSharedViewModel.setPlaceToFind(
                                    null
                                )
                            }

                            markerBottomSheetLayout.id -> {
                                restoreMarkerClickedState()
                            }

                            else -> return
                        }
                    }

                    else -> return
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /*fun convertBbox() {
        val bbox = mapView.boundingBox
        val swX = round(bbox.lonWest * 10000) / 10000
        val swY = round(bbox.latSouth * 10000) / 10000
        val neX = round(bbox.lonEast * 10000) / 10000
        val neY = round(bbox.latNorth * 10000) / 10000

        Log.d(
            "mapfragment",
            "https://www.marinetraffic.com/legacy/getxml_i?sw_x=${swX}&sw_y=${swY}&ne_x=${neX}&ne_y=${neY}&zoom=8"
        )
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLocationPermissionGranted =
            requireContext().isLocationPermissionGranted().also { granted ->
                if (granted) {
                    setEnabledLocationDrawable()
                } else setDisabledLocationDrawable()
            }

        bottomSheetLayout = binding.bottomSheetId.bottomSheetLayout
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout).apply {
            peekHeight = 400
            isHideable = true
        }

        markerBottomSheetLayout = binding.markerBottomSheetId.markerBottomSheetLayout
        markerBottomSheetBehavior = BottomSheetBehavior.from(markerBottomSheetLayout).apply {
            peekHeight = 600
            isHideable = true
        }

        measureBottomSheetLayout = binding.measureDistanceBottomSheet.measureDistanceBottomSheet
        measureBottomSheetBehavior = BottomSheetBehavior.from(measureBottomSheetLayout).apply {
            peekHeight = 400
            isHideable = false
        }

        mapView = binding.mapView.apply {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getCachedTileSource().firstOrNull()?.let { tileSourceName ->
                    mapSharedViewModel.setTile(viewModel.getCachedTileSource(tileSourceName))
                }
                setMultiTouchControls(true)
                // Deactivate zoom-in / zoom-out buttons.
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                setScrollableAreaLimitLatitude(84.4, -84.4, 0)
                minZoomLevel = 5.0

                controller.animateTo(
                    mapSharedViewModel.mapCenter ?: viewModel.getUserLocation()
                        .firstOrNull()
                    ?: GeoPoint(0.0, 0.0),
                    mapSharedViewModel.zoomLevel
                        ?: (tileProvider.tileSource.maximumZoomLevel.toDouble() - 1.0),
                    10L
                )
            }
        }
        overlayHelper = OverlayHelper(mapView, this)

        savedInstanceState?.let {
            when {
                it.getBoolean("geoPointBottomSheetVisible", false) -> {
                    mapSharedViewModel.geoPoint?.let { geoPoint ->
                        overlayHelper.addMarkerToMapAndShowBottomSheet(geoPoint)
                    }
                }

                it.getBoolean("markerBottomSheetVisible", false) -> {
                    viewModel.selectedMarker?.let { marker ->
                        openMarkerBottomSheet(marker)
                    }
                }

                it.getBoolean("measureBottomSheetVisible", false) -> {
                    // TODO()
                    Log.d(TAG(), "bundle not null")
                    customCompassView = binding.compassView
                    enterMeasureDistanceMode()
                }

                else -> {
                    return@let
                }
            }
        }

        val bitmap =
            ContextCompat.getDrawable(requireContext(), R.drawable.map_location_default)?.toBitmap()

        // Get last user's position from SharedPreferences on first start if present.
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getUserLocation().firstOrNull()?.let {
                if (mapSharedViewModel.lastKnownPosition.replayCache.firstOrNull() != null) return@let
                mapSharedViewModel.setLastKnownPosition(it)
            }
        }

        myLocationOverlay =
            object :
                CustomMyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView) {
                override fun onLocationChanged(
                    location: Location?,
                    source: IMyLocationProvider?
                ) {
                    super.onLocationChanged(location, source)
                    location ?: return

                    myLocationOverlay.runOnFirstFix {
                        GeoPoint(location.latitude, location.longitude).let { fixedGeoPoint ->
                            if (mapSharedViewModel.lastKnownPosition == fixedGeoPoint) return@runOnFirstFix
                            viewModel.saveUserLocation(fixedGeoPoint)
                            mapSharedViewModel.setLastKnownPosition(fixedGeoPoint)
                        }
                    }
                }
            }.apply {
                setPersonIcon(bitmap)
                setDirectionIcon(bitmap)
            }

        // Set the long press event listener
        rotationGestureOverlay = RotationGestureOverlay(mapView).apply {
            isEnabled = true
        }
        scaleBarOverlay = ScaleBarOverlay(mapView).apply {
            setAlignBottom(true)
            setScaleBarOffset(10, 60)
        }
        highPriorityOverlays = listOf(myLocationOverlay, copyrightOverlay, scaleBarOverlay)

        sensorListener = SensorListener(mapView)

        customCompassView = binding.compassView

        // Loads every nautical mark, current location icon,....
        mapView.overlays.addOverlay(
            listOf(
                seamarksOverlay,
                rotationGestureOverlay,
                mapEventsOverlay,
                myLocationOverlay,
                scaleBarOverlay,
                copyrightOverlay
            ), this@MapFragment
        )

        handleIntentRequest()

        customCompassView.apply {
            passMapView(mapView)
            setOnClickListener {
                if (locationButtonState != 1 && mapView.mapOrientation != 0f) {
                    mapRotationAnimation = SmoothMapOrientationAnimator(mapView).apply {
                        startAnimation(360f)
                    }
                }
            }
        }

        // Search box settings.
        binding.searchMapBox.searchMapEditText.apply {
            setOnClickListener {
                if (!util.lastClick(1000)) return@setOnClickListener
                findNavController().navigate(R.id.action_map_dest_to_searchMapFragmentDialog)
            }

            setOnTextChangedListener(object :
                CustomTextInputEditText.OnTextChangedListener {
                override fun onTextChanged(text: CharSequence?) {
                    Log.d(TAG(), text.toString())
                    if (!text.isNullOrEmpty()) {
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_close_24
                        )?.let {
                            if (drawableList.contains(it)) return
                            it.setTint(ContextCompat.getColor(requireContext(), R.color.black))
                            setDrawables(right = it)
                        }
                    } else setDrawables(right = null)
                }

            })

            setOnStartDrawableClickListener(object :
                CustomTextInputEditText.OnDrawableClickListener {
                override fun onStartDrawableClick(view: CustomTextInputEditText) {
                    (activity as MainActivity).toggleDrawer()
                }

                override fun onEndDrawableClick(view: CustomTextInputEditText) {
                    if (mapSharedViewModel.showPoiStateFlow.value != null) {
                        mapSharedViewModel.setPoiToShow(null)
                        overlayHelper.itemizedPoiIconOverlay?.let {
                            mapView.overlays.remove(it)
                            overlayHelper.resetItemizedPoiIconOverlay()
                            mapView.invalidate()
                        }
                    }

                    text = null
                    bottomSheetBehavior.state = STATE_HIDDEN
                }
            })

            setDrawables(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.baseline_menu_24
                ), null
            )

            setPaddingRelative(
                30,  // Left padding
                paddingTop,
                paddingEnd,
                paddingBottom
            )
            compoundDrawablePadding = 30

            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
            isLongClickable = false
        }

        binding.mapRightPanelLinearLayout.locationButton.setOnClickListener {
            if (!util.lastClick(300)) return@setOnClickListener

            if (requireContext().isLocationPermissionGranted()) {
                if (myLocationOverlay.myLocation == null) {
                    Toast.makeText(requireContext(), "Location not known yet.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (locationButtonState == 0 && isCentered() && mapView.zoomLevelDouble == mapView.tileProvider.tileSource.maximumZoomLevel.toDouble()) {
                    viewModel.setLocationButtonState(1)
                } else {
                    zoomAnimationJob?.cancel()
                    zoomAnimationJob = lifecycleScope.launch {
                        mapView.controller.animateTo(
                            myLocationOverlay.myLocation,
                            mapView.zoomLevelDouble,
                            null
                        )
                        if (mapView.zoomLevelDouble == mapView.tileProvider.tileSource.maximumZoomLevel.toDouble()) {
                            zoomAnimationJob = null
                            return@launch
                        }
                        delay(1200)
                        mapView.controller.animateTo(
                            myLocationOverlay.myLocation,
                            mapView.tileProvider.tileSource.maximumZoomLevel.toDouble(),
                            null
                        )
                        delay(300)
                        zoomAnimationJob = null
                    }
                }
            } else activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.setLocationButtonState(0)
                    zoomAnimationJob?.cancel()
                    zoomAnimationJob = null
                    view.performClick()
                }
            }
            mapRotationAnimation?.let {
                if (it.isAnimationStarted()) {
                    it.cancelAnimation()
                }
            }
            false
        }

        binding.mapRightPanelLinearLayout.layersButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_map_dest_to_mapBottomSheetFragment)
        }

        val sensorHandler = Handler(Looper.getMainLooper())
        val vesselHandler = Handler(Looper.getMainLooper())
        val harbourHandler = Handler(Looper.getMainLooper())
        val poiHandler = Handler(Looper.getMainLooper())

        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                saveMapState()

                sensorHandler.removeCallbacksAndMessages(null)
                sensorHandler.postDelayed({
                    if (mapView.boundingBox.contains(
                            myLocationOverlay.myLocation ?: return@postDelayed
                        )
                    ) {
                        sensorListener.registerSensorListener()
                    } else sensorListener.unregisterSensorListener()
                }, 200)

                if (viewModel.vesselLocationsVisible.value == true) {
                    vesselHandler.removeCallbacksAndMessages(null)
                    vesselHandler.postDelayed({
                        viewModel.emitVisibleVessels(mapView.boundingBox)
                    }, 200)
                }

                /* if (viewModel.harboursVisible.value == true) {
                     harbourHandler.removeCallbacksAndMessages(null)
                     harbourHandler.postDelayed(
                         {
                             harboursDatabaseRepositoryViewModel.emitVisibleHarbours(mapView.boundingBox)
                         }, 200
                     )
                 }*/

                if (mapView.zoomLevelDouble >= 17) {
                    poiHandler.removeCallbacksAndMessages(null)
                    poiHandler.postDelayed(
                        {
                            if (mapSharedViewModel.showPoiStateFlow.value != null) return@postDelayed
                            if (searchForPoisJob?.isCompleted == false) return@postDelayed

                            searchForPoisJob =
                                viewLifecycleOwner.lifecycleScope.launch {
                                    viewModel.filterPoiByBoundingBox(mapView.boundingBox)
                                        .firstOrNull().takeIf { !it.isNullOrEmpty() }?.let {
                                            overlayHelper.addPoiToMap(it)
                                        }
                                }
                        }, 300
                    )
                } else {
                    if (mapSharedViewModel.showPoiStateFlow.value != null) return true
                    // Cancel pending jobs
                    if (searchForPoisJob != null) {
                        poiHandler.removeCallbacksAndMessages(null)
                        searchForPoisJob?.cancel()
                        searchForPoisJob = null
                    }

                    overlayHelper.itemizedPoiIconOverlay?.let {
                        mapView.overlays.remove(it)
                        overlayHelper.resetItemizedPoiIconOverlay()
                        mapView.invalidate()
                    }
                }

                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                saveMapState()
                return false
            }
        })

        binding.mapLeftPanelLinearLayout.customizeMapImageButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_map_dest_to_customizeMapDialog)
        }

        binding.bottomSheetId.addMarkerButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.customMarkerDialog)
        }

        binding.bottomSheetId.measureDistanceButton.setOnClickListener {
            enterMeasureDistanceMode()
        }

        binding.measureDistanceTop.measureDistanceBackButton.setOnClickListener {
            leaveMeasureDistanceMode()
        }

        binding.measureDistanceTop.undoDistanceLine.setOnClickListener {
            if (overlayHelper.geoPointList.size > 1) {
                mapView.overlays.apply {
                    remove(overlayHelper.itemizedMarkerIconList[overlayHelper.itemizedMarkerIconList.size - 1])
                    remove(overlayHelper.distanceLinePaths[overlayHelper.distanceLinePaths.size - 1])
                }
                overlayHelper.setItemizedIconArray(
                    overlayHelper.itemizedMarkerIconList.subList(
                        0,
                        overlayHelper.itemizedMarkerIconList.size - 1
                    )
                )
                overlayHelper.overwriteDistanceLinePaths(
                    overlayHelper.distanceLinePaths.subList(
                        0,
                        overlayHelper.distanceLinePaths.size - 1
                    )
                )
                overlayHelper.setGeoPointList(
                    overlayHelper.geoPointList.subList(
                        0,
                        overlayHelper.geoPointList.size - 1
                    )
                )
                overlayHelper.calculateDistance()
                mapView.invalidate()
            }
        }

        binding.measureDistanceTop.clearDistanceLines.setOnClickListener {
            if (overlayHelper.geoPointList.size > 1) {
                mapView.overlays.removeAll(overlayHelper.distanceLinePaths)
                overlayHelper.clearDistanceLinePaths()
                mapView.overlayManager.overlays()
                    .removeAll(
                        overlayHelper.itemizedMarkerIconList.subList(
                            1,
                            overlayHelper.itemizedMarkerIconList.size
                        )
                    )
                overlayHelper.setItemizedIconArray(listOf(overlayHelper.itemizedMarkerIconList[0]))
                overlayHelper.setGeoPointList(listOf(overlayHelper.geoPointList[0]))
                overlayHelper.calculateDistance()
                mapView.invalidate()
            }
        }

        binding.measureDistanceBottomSheet.addTargetButton.setOnClickListener {
            // Calculate the screen position of the center
            val center = Point(mapView.width / 2, mapView.height / 2)
            // Calculate the geographical position of the center
            val centerGeoPoint = mapView.projection?.fromPixels(center.x, center.y) as GeoPoint
            if (!overlayHelper.geoPointList.contains(centerGeoPoint)) {
                overlayHelper.setGeoPointList(overlayHelper.geoPointList + centerGeoPoint)
                overlayHelper.drawDistancePaths(overlayHelper.geoPointList)
                //overlayHelper.addMarkerToMap(centerGeoPoint)
                overlayHelper.addMeasurePoint(centerGeoPoint)
            }
        }

        binding.bottomSheetId.shareLocationButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            val action = MapFragmentDirections.actionMapDestToMapShareBottomSheetDialog(
                floatArrayOf(
                    overlayHelper.geoPointList[0].latitude.toFloat(),
                    overlayHelper.geoPointList[0].longitude.toFloat()
                )
            )
            findNavController().navigate(action)
        }

        binding.bottomSheetId.toolsButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_map_dest_to_mapToolsBottomSheetFragment)
        }

        mapSharedViewModel.tileSource.observe(viewLifecycleOwner) {
            if (it == CustomTileSourceFactory.SAT) {
                overlayHelper.addLandBoundariesLayer()
            }
            if (mapView.overlays.contains(overlayHelper.getLandBoundariesOverlay()) && mapSharedViewModel.tileSource.value != CustomTileSourceFactory.SAT) {
                mapView.overlays.remove(overlayHelper.getLandBoundariesOverlay())
            }
            // Clear the current tile cache
            mapView.apply {
                tileProvider.clearTileCache()
                val previousOverlay = this.overlayManager.tilesOverlay
                overlayManager.remove(previousOverlay)
                setTileSource(mapSharedViewModel.tileSource.value)
                maxZoomLevel = mapView.tileProvider.tileSource.maximumZoomLevel.toDouble() + 2
                invalidate()
            }
        }

        mapSharedViewModel.gridOverlayVisible.observe(viewLifecycleOwner) {
            if (it && gridOverlay == null) {
                gridOverlay = LatLonGridlineOverlay2()
                gridOverlay?.textPaint = Paint(Color.TRANSPARENT)
                mapView.overlays.addOverlay(gridOverlay, this)

                mapView.invalidate()
            }

            if (!it && gridOverlay != null) {
                mapView.overlays.removeAt(mapView.overlays.lastIndexOf(gridOverlay))
                gridOverlay = null
                mapView.invalidate()
            }
        }

        viewModel.harboursVisible.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    /*overlayHelper.harboursClusterer ?: overlayHelper.initHarboursClusterer()
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (harboursDatabaseRepositoryViewModel.isHarboursDatabaseEmpty.firstOrNull() == true) {
                            viewModel.fetchHarbours()
                            return@launch
                        }
                        harboursDatabaseRepositoryViewModel.emitVisibleHarbours(mapView.boundingBox)
                    }*/
                }

                false -> {
                    if (mapView.overlays.contains(overlayHelper.harboursClusterer)) {
                        overlayHelper.cancelHarboursLoadingJob()
                        mapView.overlayManager.remove(overlayHelper.harboursClusterer)
                        overlayHelper.deinitHarboursClusterer()
                        mapView.invalidate()
                    }
                }
            }
        }

        viewModel.vesselLocationsVisible.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    vesselsClusterer ?: let {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.fetchVesselsLocation()
                        }
                    }
                }

                false -> {
                    if (vesselsClusterer != null) {
                        mapView.overlayManager.overlays().remove(vesselsClusterer)
                        viewModel.apply {
                            saveVesselsMarkers(null)
                            setSelectedMarker(null)
                        }
                        vesselsClusterer = null
                        mapView.invalidate()
                    }
                    vesselLoadingJob = null
                }
            }
        }

        if (!isLocationPermissionGranted) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    mapSharedViewModel.permissionGranted.collect { granted ->
                        Log.d(TAG(), "isGranted: $granted")
                        granted ?: return@collect
                        if (granted) {
                            isLocationPermissionGranted = granted
                            setEnabledLocationDrawable()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCustomizeDialogVisible.collect {
                    when (it) {
                        true -> {
                            binding.searchMapBox.searchBoxLinearLayout.visibility = View.GONE
                            binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility = View.GONE
                            binding.mapRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
                                View.GONE
                            binding.compassView.visibility = View.GONE
                        }

                        false -> {
                            binding.searchMapBox.searchBoxLinearLayout.visibility = View.VISIBLE
                            binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility =
                                View.VISIBLE
                            binding.mapRightPanelLinearLayout.mapButtonsLayerLinearLayout.visibility =
                                View.VISIBLE
                            binding.compassView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.loadAllCustomPoi.collect {
                    overlayHelper.drawCustomPoiOnMap(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.placeToFindStateFlow.collect {
                    it ?: return@collect

                    val geoPoint = GeoPoint(it.latitude, it.longitude)

                    it.polygonCoordinates.takeIf { coordinates -> coordinates.isNotEmpty() }
                        ?.let { encodedPolygon ->
                            PolylineAlgorithms().decode(encodedPolygon).let { polylineList ->
                                overlayHelper.drawPolygon(polylineList)
                                mapView.zoomToBoundingBox(
                                    BoundingBox.fromGeoPoints(polylineList),
                                    true,
                                    100
                                )
                            }
                        } ?: run {
                        mapView.controller.animateTo(geoPoint, 13.0, 1000L)
                    }

                    overlayHelper.addMarkerToMapAndShowBottomSheet(geoPoint)
                    binding.searchMapBox.searchMapEditText.setText(it.name)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // TODO("CREATED")
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.showPoiStateFlow.collect { poiMap ->
                    if (poiMap.isNullOrEmpty()) return@collect
                    // poiMap holds category name and List of IDs which are used for search in database.
                    viewModel.searchInCache(poiMap.values.flatten()).let { flow ->
                        flow.firstOrNull()?.let { poiEntityList ->
                            binding.searchMapBox.searchMapEditText.setText(
                                poiMap.keys.toList().firstOrNull()
                            )
                            overlayHelper.addPoiToMap(poiEntityList)
                            val boundingBox = BoundingBox.fromGeoPoints(poiEntityList.map {
                                GeoPoint(
                                    it.latitude,
                                    it.longitude
                                )
                            })
                            mapView.zoomToBoundingBox(boundingBox, true, 400)
                        }
                    }
                }
            }
        }

        /*viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                harboursDatabaseRepositoryViewModel.loadAllHarbours.collect { harbours ->
                    if (viewModel.harboursVisible.value == true && harbours.isNotEmpty()) {
                        overlayHelper.drawHarbourMarkerOnMap(harbours)
                    }
                }
            }
        }*/

        // Changes location button state, if state == 1 mapView will be rotated based on current
        // device's direction.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationButtonStateFlow.collect {
                    it ?: return@collect

                    locationButtonState = it
                    setEnabledLocationDrawable()
                    myLocationOverlay.setDrawState(it)
                    sensorListener.buttonState(it)
                }
            }
        }

        /* viewLifecycleOwner.lifecycleScope.launch {
             viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                 viewModel.harboursFetchingState.collect { state ->
                     state ?: return@collect
                     if (viewModel.harboursVisible.value != true) return@collect

                     if (state.error != null) {
                         findNavController().popBackStack(
                             R.id.customizeMapDialog,
                             false
                         )
                     }

                     when (state.error) {
                         UiState.Error.NetworkError -> {
                             Toast.makeText(
                                 requireContext(),
                                 getString(R.string.no_network_available),
                                 Toast.LENGTH_SHORT
                             ).show()
                             viewModel.toggleHarboursLocations()
                         }

                         UiState.Error.ServiceUnavailable -> {
                             Toast.makeText(
                                 requireContext(),
                                 getString(R.string.service_unavailable),
                                 Toast.LENGTH_SHORT
                             ).show()
                             viewModel.toggleHarboursLocations()
                         }

                         UiState.Error.Other -> {
                             Toast.makeText(
                                 requireContext(),
                                 getString(R.string.something_went_wrong),
                                 Toast.LENGTH_SHORT
                             ).show()
                             viewModel.toggleHarboursLocations()
                         }

                         null -> {
                             if (state.isLoading) {
                                 if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                     val action =
                                         CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                             getString(R.string.loading_harbours_text)
                                         )
                                     findNavController().navigate(action)
                                 }
                             } else {
                                 if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                     findNavController().popBackStack(R.id.customizeMapDialog, false)
                                 }
                             }

                             state.items.takeIf { it.isNotEmpty() }?.let {
                                 it.map { id -> id.harborId }.let { idList ->
                                     if (harboursDatabaseRepositoryViewModel.isHarbourIdInDatabase(
                                             idList
                                         )
                                             .firstOrNull() == false
                                     ) {
                                         harboursDatabaseRepositoryViewModel.addHarbours(it)
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }*/

        /*viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                harboursDatabaseRepositoryViewModel.harboursInAreaSharedFlow.collect { harbourInfoList ->
                    if (viewModel.harboursVisible.value == false) return@collect

                    if (!harbourInfoList.isNullOrEmpty()) {
                        overlayHelper.drawHarbourMarkerOnMap(
                            harbourInfoList,
                            viewModel.selectedMarker?.id
                        )
                        overlayHelper.harboursClusterer?.getItems().let markerList@{ itemList ->
                            itemList ?: return@markerList
                            if (itemList.isEmpty()) return@markerList
                        }
                    } else {
                        viewModel.fetchHarbours(mapView.boundingBox, mapView.zoomLevelDouble)
                    }
                }
            }
        }*/

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.vesselsFetchingState.collect { state ->
                    state ?: return@collect
                    if (viewModel.vesselLocationsVisible.value != true) return@collect

                    if (state.error != null) {
                        findNavController().popBackStack(
                            R.id.customizeMapDialog,
                            false
                        )
                    }

                    when (state.error) {
                        UiState.Error.NetworkError -> {
                            if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.no_network_available),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.toggleVesselLocations()
                            }
                        }

                        UiState.Error.ServiceUnavailable -> {
                            if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.service_unavailable),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.toggleVesselLocations()
                            }
                        }

                        UiState.Error.Other -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.toggleVesselLocations()
                        }

                        null -> {
                            if (state.isLoading) {
                                if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                    val action =
                                        CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                            getString(R.string.loading_vessels_text)
                                        )
                                    findNavController().navigate(action)
                                }
                            } else {
                                if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                    findNavController().popBackStack(R.id.customizeMapDialog, false)
                                }
                                drawVesselsOnMap(state.items)
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vesselsInAreaSharedFlow.collect { visibleVessels ->
                    if (viewModel.vesselLocationsVisible.value == false || visibleVessels.isEmpty()) return@collect

                    vesselsClusterer?.let { clusterer ->
                        clusterer.clearItems()

                        vesselLoadingJob?.cancel()
                        vesselLoadingJob = lifecycleScope.launch(Dispatchers.Default) {
                            val vesselList = visibleVessels.mapAsync { vesselInfo ->
                                val geoPoint =
                                    GeoPoint(
                                        vesselInfo.latitude.toDouble(),
                                        vesselInfo.longitude.toDouble()
                                    )
                                val marker = CustomMarker(mapView).apply marker@{
                                    setOnMarkerClickListener(this@MapFragment)
                                    id = vesselInfo.id
                                    // If selected marker id is equal to currently drawn, set isClicked to true
                                    if (viewModel.selectedMarker?.id == vesselInfo.id) {
                                        isClicked(true)
                                    }
                                    setMarkerType(CustomMarker.CustomMarkerType.VesselMarker)
                                    rotation =
                                        if (vesselInfo.heading.isEmpty()) 0f else vesselInfo.heading.toFloat()
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    setDrawableId(R.drawable.vessel_marker)
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.vessel_marker
                                    )?.apply {
                                        ContextCompat.getColor(
                                            requireContext(),
                                            MapUtil.determineVesselColorType(vesselInfo.type)
                                        ).let { color ->
                                            setTint(color)
                                            setDrawableColor(color)
                                        }
                                        icon = this
                                    }
                                    position = geoPoint
                                }

                                viewModel.selectedMarker?.id?.let id@{ id ->
                                    if (marker.id != id) return@id
                                    mapView.projection.toPixels(marker.position, null)
                                        .let { point ->
                                            mapView.performLongClick(
                                                point.x.toFloat(),
                                                point.y.toFloat()
                                            )
                                        }
                                }
                                marker
                            }
                            clusterer.apply {
                                addAll(vesselList)
                                viewModel.saveVesselsMarkers(vesselList)
                                invalidate()
                            }
                            mapView.invalidate()
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO( WHY ? )
        if (findNavController().currentDestination?.id != R.id.map_dest) return
        when {
            bottomSheetLayout.isVisible -> {
                outState.putBoolean("geoPointBottomSheetVisible", bottomSheetLayout.isVisible)
            }

            markerBottomSheetLayout.isVisible -> {
                outState.putBoolean("markerBottomSheetVisible", markerBottomSheetLayout.isVisible)
            }

            measureBottomSheetLayout.isVisible -> {
                outState.putBoolean("measureBottomSheetVisible", measureBottomSheetLayout.isVisible)
            }
        }
    }

    // Find selected marker and restore it's clicked state.
    private fun restoreMarkerClickedState() {
        viewModel.selectedMarker?.let { selectedMarker ->
            when (selectedMarker.type) {
                /*CustomMarker.CustomMarkerType.HarbourMarker -> {
                    overlayHelper.harboursClusterer?.getItems()
                        ?.firstOrNull { marker -> marker.id == selectedMarker.id }
                        ?.isClicked(false)
                }*/

                CustomMarker.CustomMarkerType.VesselMarker -> {
                    vesselsClusterer?.getItems()
                        ?.firstOrNull { marker -> marker.id == selectedMarker.id }
                        ?.isClicked(false)
                }

                CustomMarker.CustomMarkerType.CustomPoi -> {
                    overlayHelper.customPoiMarkerList.firstOrNull { marker -> marker.id == selectedMarker.id }
                        ?.isClicked(false)
                }
            }
            // Clear variable which holds last clicked marker.
            viewModel.setSelectedMarker(null)
        }
    }

    private fun drawVesselsOnMap(vesselEntity: List<VesselInfoEntity>) {
        if (viewModel.vesselLocationsVisible.value == true) {
            vesselsClusterer = VesselRadiusMarkerClusterer(requireContext()).apply {
                viewModel.vesselMarkers?.forEach {
                    add(it)
                }
                setRadius(160)
                setMaxClusteringZoomLevel(7.0)
            }

            if (viewModel.vesselMarkers.isNullOrEmpty()) viewModel.emitVisibleVessels(
                mapView.boundingBox,
                vesselEntity
            )

            mapView.overlayManager.apply {
                addOverlay(vesselsClusterer, this@MapFragment)

                if (contains(overlayHelper.harboursClusterer)) {
                    swap(vesselsClusterer, overlayHelper.harboursClusterer)
                }
            }
        }
    }

    private fun saveMapState() {
        mapSharedViewModel.apply {
            setCenterPoint(mapView.mapCenter)
            setZoomLevel(mapView.zoomLevelDouble)
        }
    }

    private fun isCentered(): Boolean {
        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let { geoPoint ->
            val inCenter: Boolean
            val currentLocationScreenPoint: Point =
                mapView.projection.toPixels(geoPoint, null)
            val mapCenterScreenPoint: Point =
                mapView.projection.toPixels(mapView.mapCenter, null)
            // Define a threshold for closeness (adjust as needed)
            val threshold = 10
            val isIconInCenter: Boolean =
                kotlin.math.abs(currentLocationScreenPoint.x - mapCenterScreenPoint.x) <= threshold &&
                        kotlin.math.abs(currentLocationScreenPoint.y - mapCenterScreenPoint.y) <= threshold
            inCenter = isIconInCenter
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
                        R.drawable.baseline_location_disabled_24
                    )
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
                0 -> {
                    locationButton
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.baseline_location_searching_24
                            )
                        )
                }

                1 -> {
                    locationButton
                        .setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.baseline_my_location_24
                            )
                        )
                    myLocationOverlay.enableFollowLocation()
                }
            }
        }
    }

    private fun handleIntentRequest() {
        mapSharedViewModel.getIntentCoordinates()?.let { geoPoint ->
            overlayHelper.addMarkerToMapAndShowBottomSheet(geoPoint)
            mapView.controller.animateTo(mapSharedViewModel.getIntentCoordinates() as IGeoPoint)
            mapSharedViewModel.getIntentZoom()?.let { zoom ->
                mapView.controller.zoomTo(
                    when {
                        zoom > mapView.tileProvider.maximumZoomLevel -> mapView.tileProvider.maximumZoomLevel.toDouble()
                        zoom < mapView.tileProvider.minimumZoomLevel -> mapView.tileProvider.minimumZoomLevel.toDouble()
                        else -> zoom
                    }
                )
            }
        }
    }

    private fun enterMeasureDistanceMode() {
        overlayHelper.setIsMeasuring(true)
        measureBottomSheetLayout.apply {
            binding.bottomSheetId.bottomSheetLayout.visibility = View.GONE
            visibility = View.VISIBLE
            //startAnimation(slideAnimation)
        }

        binding.mapRightPanelLinearLayout.layersButton.visibility = View.GONE
        binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility = View.GONE

        val layoutParams =
            binding.mapRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 65

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.VISIBLE

        customCompassView.setCompassCenterXY(30, 80)

        overlayHelper.addTargetOverlay()
        overlayHelper.addMeasurePoint(overlayHelper.geoPointList[0])
        mapView.controller.animateTo(overlayHelper.geoPointList[0])
    }

    private fun leaveMeasureDistanceMode() {
        overlayHelper.setIsMeasuring(false)
        mapView.controller.animateTo(overlayHelper.geoPointList[0])

        binding.measureDistanceTop.measureDistanceTopLinearLayout.visibility = View.GONE
        measureBottomSheetLayout.visibility = View.GONE
        binding.mapRightPanelLinearLayout.layersButton.visibility = View.VISIBLE
        binding.mapLeftPanelLinearLayout.leftLinearLayout.visibility = View.VISIBLE
        bottomSheetLayout.apply {
            visibility = View.VISIBLE
            startAnimation(slideAnimation)
        }
        bottomSheetBehavior.state = STATE_EXPANDED

        (binding.mapRightPanelLinearLayout.locationButton.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = 0
        }

        mapView.overlays.apply {
            remove(overlayHelper.getTargetOverlay())
            removeAll(overlayHelper.distanceLinePaths)
            removeAll(overlayHelper.itemizedMarkerIconList)
        }
        overlayHelper.clearDistanceLinePaths()

        if (overlayHelper.geoPointList.size > 1) {
            overlayHelper.setGeoPointList(listOf(overlayHelper.geoPointList[0]))
        }

        binding.measureDistanceBottomSheet.distanceTextview.text = ""
        overlayHelper.addMarkerToMapAndShowBottomSheet(overlayHelper.geoPointList[0])
        mapView.controller.animateTo(overlayHelper.geoPointList[0])
        customCompassView.setCompassCenterXY(30, 30)
    }

    private fun openBottomSheetLayout(p: GeoPoint) {
        shouldInterceptBackEvent = true

        mapSharedViewModel.setGeoPoint(p)
        if (!bottomSheetLayout.isVisible) {

            binding.bottomSheetId.coordinates.text = getString(
                R.string.split_two_strings_formatter,
                MapUtil.latitudeToDMS(p.latitude),
                MapUtil.longitudeToDMS(p.longitude)
            )

            bottomSheetLayout.visibility = View.VISIBLE
            bottomSheetBehavior.state = STATE_EXPANDED
            // Load the slide-up animation
            slideAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            bottomSheetLayout.startAnimation(slideAnimation)
        } else binding.bottomSheetId.coordinates.text = getString(
            R.string.split_two_strings_formatter,
            MapUtil.latitudeToDMS(p.latitude),
            MapUtil.longitudeToDMS(p.longitude)
        )
    }

    @SuppressLint("ResourceType")
    override fun openMarkerBottomSheet(marker: CustomMarker) {
        if (marker.id != viewModel.selectedMarker?.id || marker.type != viewModel.selectedMarker?.type) restoreMarkerClickedState()
        shouldInterceptBackEvent = true

        lifecycleScope.launch {
            marker.id ?: return@launch
            async {
                when (marker.type) {
                    // Get marker info of the type VesselMarker.
                    CustomMarker.CustomMarkerType.VesselMarker -> {
                        viewModel.searchVesselById(marker.id.toInt())
                            .firstOrNull()
                            ?.let { vesselInfo ->
                                marker.apply {
                                    val sdf = SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm",
                                        Locale.getDefault()
                                    ).apply {
                                        timeZone = TimeZone.getTimeZone("UTC")
                                    }
                                    title = vesselInfo.name + " (${vesselInfo.flag})"
                                    val locale = Locale("", vesselInfo.flag)
                                    subDescription =
                                        resources.getString(R.string.vessel_info_description)
                                            .format(
                                                locale.displayCountry,
                                                StringUtil.formatNumberWithSpaces(vesselInfo.mmsi.toInt()),
                                                when (vesselInfo.length.isEmpty()) {
                                                    true -> "--"
                                                    else -> vesselInfo.length + " m"
                                                },
                                                (vesselInfo.speed.takeIf { speed -> speed.isNotEmpty() }
                                                    ?.toDouble()
                                                    ?.div(10.0)) ?: "--",
                                                if (vesselInfo.heading.isEmpty()) "--" else vesselInfo.heading + "°",
                                                getString(
                                                    MapUtil.determineVesselType(
                                                        vesselInfo.type,
                                                    )
                                                ),
                                                if (vesselInfo.eta.isNotEmpty()) sdf.format(
                                                    vesselInfo.timeStamp - (vesselInfo.eta.toLong() * 60000L)
                                                ) else "--"
                                            )
                                }
                            }
                    }

                    // Get marker info of the type CustomPoi.
                    CustomMarker.CustomMarkerType.CustomPoi -> {
                        viewModel.searchCustomPoiById(marker.id.toInt())
                            .firstOrNull()
                            ?.let { customPoiInfo ->
                                marker.apply {
                                    title = customPoiInfo.poiName
                                    setDrawableResourceName(customPoiInfo.drawableResourceName)
                                    subDescription = customPoiInfo.description
                                }
                            }
                    }

                    // Get marker info of the type HarbourMarker.
                    /*CustomMarker.CustomMarkerType.HarbourMarker -> {
                        viewModel.searchHarbourById(marker.id.toInt())
                            .firstOrNull()
                            ?.let { harbourInfo ->
                                marker.apply {
                                    title = harbourInfo.harborName
                                }
                            }
                    }*/
                }
            }.await().let { customMarker ->
                // Perform UI operations based on received result.
                customMarker ?: return@launch
                if (bottomSheetLayout.isVisible) bottomSheetBehavior.state = STATE_HIDDEN
                //
                viewModel.setSelectedMarker(customMarker)

                binding.markerBottomSheetId.apply {
                    markerBottomSheetTitle.text = customMarker.title
                    customMarker.subDescription.let { description ->
                        markerBottomSheetDescriptionTextView.apply {
                            if (!description.isNullOrEmpty()) {
                                text = customMarker.subDescription
                                visibility = View.VISIBLE
                            } else visibility = View.GONE
                        }
                    }
                    markerBottomSheetImageView.setImageDrawable(
                        when (customMarker.drawableId) {
                            null -> ContextCompat.getDrawable(
                                requireContext(),
                                resources.getIdentifier(
                                    customMarker.drawableResourceName,
                                    "drawable",
                                    requireActivity().packageName
                                )
                            )

                            else -> {
                                customMarker.drawableId?.let {
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        it
                                    )?.apply {
                                        customMarker.drawableColor?.let { color ->
                                            setTint(color)
                                        }
                                    }
                                }
                            }
                        }
                    )

                    markerBottomSheetGeopointTextView.text =
                        resources.getString(R.string.split_two_strings_formatter).format(
                            MapUtil.latitudeToDMS(customMarker.position.latitude),
                            MapUtil.longitudeToDMS(customMarker.position.longitude)
                        )
                    if (myLocationOverlay.myLocation != null) {
                        markerBottomSheetDistanceTextView.apply {
                            if (visibility != View.VISIBLE) visibility = View.VISIBLE
                            text =
                                resources.getString(R.string.distance_from_geopoint).format(
                                    StringUtil.formatDistanceDoubleToString(
                                        GeoPoint(customMarker.position).distanceToAsDouble(
                                            myLocationOverlay.myLocation
                                                ?: mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                        )
                                    )
                                )
                        }
                        markerBottomSheetBearingTextView.apply {
                            if (visibility != View.VISIBLE) visibility = View.VISIBLE
                            text =
                                resources.getString(R.string.bearing_from_geopoint).format(
                                    StringUtil.formatBearingDegrees(
                                        GeoPoint(myLocationOverlay.myLocation).bearingTo(
                                            customMarker.position
                                        )
                                    )
                                )
                        }
                    } else {
                        markerBottomSheetDistanceTextView.visibility = View.GONE
                        markerBottomSheetBearingTextView.visibility = View.GONE
                    }
                }
                slideAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

                markerBottomSheetBehavior.apply {
                    state = STATE_EXPANDED
                    markerBottomSheetLayout.apply {
                        startAnimation(slideAnimation)
                        visibility = View.VISIBLE
                    }
                }
            }
        }
        mapView.invalidate()
    }

    override fun markerToMapAdded(geoPoint: GeoPoint) {
        openBottomSheetLayout(geoPoint)
        binding.searchMapBox.searchMapEditText.setText(
            getString(R.string.split_two_strings_formatter).format(
                MapUtil.latitudeToDMS(geoPoint.latitude),
                MapUtil.longitudeToDMS(geoPoint.longitude)
            )
        )
    }

    private fun closeMarkerBottomSheet() {
        slideAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        slideAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                markerBottomSheetLayout.visibility = View.GONE
                markerBottomSheetBehavior.state = STATE_HIDDEN
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        markerBottomSheetLayout.startAnimation(slideAnimation)
    }

    override fun updateDistanceTextView(text: String) {
        binding.measureDistanceBottomSheet.distanceTextview.text = text
    }

    override fun overlayAddedListener() {
        if (!::scaleBarOverlay.isInitialized) return
        if (mapView.overlays.indexOf(scaleBarOverlay) != mapView.overlays.size - 1) {
            mapView.overlays.removeAll(highPriorityOverlays)
            mapView.overlays.addOverlay(highPriorityOverlays, this)
            mapView.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        getInstance().load(
            requireActivity(),
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
        )
        sensorListener.registerSensorListener()
        myLocationOverlay.enableMyLocation()

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        markerBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        sensorListener.unregisterSensorListener()
        myLocationOverlay.disableMyLocation()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        markerBottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)

        vesselLoadingJob?.cancel()
        vesselLoadingJob = null
        overlayHelper.cancelHarboursLoadingJob()

        saveMapState()
        myLocationOverlay.myLocation?.let {
            mapSharedViewModel.setLastKnownPosition(it)
        }

        mapView.onPause()
    }
}