package com.bytecause.download_tiles.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.download_tiles.CustomTileSourceFactory
import com.bytecause.features.download_tiles.R
import com.bytecause.features.download_tiles.databinding.DownloadTilesFragmentLayoutBinding
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.context.storageAvailable
import com.bytecause.util.delegates.viewBinding
import com.caverock.androidsvg.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import kotlin.properties.Delegates

@AndroidEntryPoint
class DownloadMapFragment : Fragment(R.layout.download_tiles_fragment_layout) {
    private val binding by viewBinding(DownloadTilesFragmentLayoutBinding::bind)

    private lateinit var customMapView: CustomMapView

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private var zoomLevel by Delegates.notNull<Double>()

    private val lastClick = com.bytecause.util.common.LastClick()

    private var offlineManager: OfflineManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Configuration.getInstance().load(
            requireContext().applicationContext,
            PreferenceManager.getDefaultSharedPreferences(requireActivity()),
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        offlineManager = OfflineManager.getInstance(requireContext())

        zoomLevel = mapSharedViewModel.cameraPosition?.zoom ?: 6.0

        customMapView =
            binding.mapWindow.apply {
                maxZoomLevel = this.tileProvider.tileSource.maximumZoomLevel.toDouble()
                minZoomLevel = 6.0
                controller.setZoom(
                    when {
                        this@DownloadMapFragment.zoomLevel < 6.0 -> 6.0
                        this@DownloadMapFragment.zoomLevel >= 6.0 && this@DownloadMapFragment.zoomLevel < maxZoomLevel -> this@DownloadMapFragment.zoomLevel
                        else -> this@DownloadMapFragment.zoomLevel - ((this@DownloadMapFragment.zoomLevel - maxZoomLevel))
                    },
                )
                controller.setCenter(
                    mapSharedViewModel.cameraPosition?.target?.run {
                        GeoPoint(latitude, longitude)
                    } ?: GeoPoint(0.0, 0.0),
                )
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                setMultiTouchControls(true)
                invalidate()

                setOnTouchListener { view2, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_MOVE -> {
                            setTotalTilesByArea()
                        }
                    }
                    view2.performClick()
                }
            }
        setTileSource()

        val zoomHandler = Handler(Looper.getMainLooper())
        customMapView.addMapListener(
            object : MapListener {
                override fun onScroll(event: ScrollEvent): Boolean {
                    zoomHandler.removeCallbacksAndMessages(null)
                    zoomHandler.postDelayed({
                        setSlidePrefs(customMapView.zoomLevelDouble.toInt())
                        mapSharedViewModel.saveCameraPosition(
                            CameraPosition.Builder()
                                .zoom(customMapView.zoomLevelDouble)
                                .target(
                                    customMapView.mapCenter.run {
                                        LatLng(latitude, longitude)
                                    },
                                )
                                .build(),
                        )
                        // mapSharedViewModel.setZoomLevel(customMapView.zoomLevelDouble)
                    }, 100)
                    return true
                }

                override fun onZoom(event: ZoomEvent): Boolean {
                    setTotalTilesByArea()
                    return false
                }
            },
        )

        storageAvailable()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.tileSource.collect {
                    it ?: return@collect

                    binding.currentTileSourceTextView.text =
                        when (it) {
                            is TileSources.Raster.Default -> {
                                when (it.id) {
                                    DefaultTileSources.OPEN_TOPO.id -> "Topography"
                                    DefaultTileSources.SATELLITE.id -> "Satellite"
                                    DefaultTileSources.MAPNIK.id -> "Mapnik"
                                    else -> ""
                                }
                            }

                            else -> {
                                ""
                            }
                        }
                    setTileSource()
                    customMapView.invalidate()

                    if (customMapView.zoomLevelDouble > customMapView.tileProvider.tileSource.maximumZoomLevel) {
                        customMapView.apply {
                            controller.setZoom((customMapView.tileProvider.tileSource.maximumZoomLevel).toDouble())
                            maxZoomLevel =
                                customMapView.tileProvider.tileSource.maximumZoomLevel.toDouble()
                        }
                        // mapSharedViewModel.setZoomLevel(customMapView.zoomLevelDouble)
                    }
                    setSlidePrefs(customMapView.zoomLevelDouble.toInt())
                    lifecycleScope.launch(Dispatchers.Main) {
                        setTotalTilesByArea()
                    }
                }
            }
        }

        binding.mapTileSourceType.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_downloadMapFragment_to_tileSourceTypes)
        }

        binding.toolbar.navBack.setOnClickListener {
            //  findNavController().popBackStack(R.id.map_dest, false)
            findNavController().popBackStack()
        }

        binding.toolbar.destNameTextView.text =
            findNavController().currentDestination?.label.toString()

        binding.availableStorage.availableStorageRefreshButton.setOnClickListener {
            storageAvailable()
        }

        binding.zoomRangeSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser && slider.values.indexOf(value) == 0) {
                // Only perform zoom if the change was from the first thumb
                customMapView.controller.setZoom(value.toInt().toDouble())
                binding.minZoomLevelTextView.text = value.toInt().toString()
                setTotalTilesByArea()
            }
            if (fromUser && slider.values.indexOf(value) == 1) {
                setSlidePrefs(customMapView.zoomLevelDouble.toInt())
                setTotalTilesByArea()
            }
        }

        binding.downloadTilesButton.setOnClickListener {

            val tileSourceUrl = "asset://style.json"

            val bounds = customMapView.boundingBox.run {
                LatLngBounds.from(
                    latNorth = latNorth,
                    lonEast = lonEast,
                    latSouth = latSouth,
                    lonWest = lonWest
                )
            }
            val minZoom = customMapView.minZoomLevel
            val maxZoom = customMapView.maxZoomLevel

            OfflineTilePyramidRegionDefinition

            val definition = OfflineTilePyramidRegionDefinition(
                tileSourceUrl,
                bounds,
                minZoom,
                maxZoom,
                requireContext().resources.displayMetrics.density
            )

            val metadata: ByteArray = "{}".toByteArray() // Example metadata

            offlineManager?.createOfflineRegion(
                definition,
                metadata,
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: OfflineRegion) {
                        // Download the region
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                            override fun onStatusChanged(status: OfflineRegionStatus) {
                                if (status.isComplete) {
                                    // Download complete
                                    Log.d("OfflineRegion", "Region downloaded successfully.")
                                } else {
                                    // Download progress
                                    val percentage = if (status.requiredResourceCount >= 0) {
                                        100.0 * status.completedResourceCount / status.requiredResourceCount
                                    } else {
                                        0.0
                                    }
                                    Log.d("OfflineRegion", "Download progress: $percentage%")
                                }
                            }

                            override fun onError(error: OfflineRegionError) {
                                Log.e("OfflineRegion", "Error: ${error.reason}")
                            }

                            override fun mapboxTileCountLimitExceeded(limit: Long) {
                                Log.e("OfflineRegion", "Tile count limit exceeded: $limit")
                            }
                        })
                    }

                    override fun onError(error: String) {
                        Log.e("OfflineRegion", "Error: $error")
                    }
                })


            /*
                        if (!lastClick.lastClick(1000)) return@setOnClickListener

                        if (mapSharedViewModel.tileSource.value != CustomTileSourceFactory.SAT &&
                            mapSharedViewModel.tileSource.value != TileSourceFactory.MAPNIK
                        ) {
                            CacheDownloaderArchive(
                                binding.minZoomLevelTextView.text.toString().toInt(),
                                binding.maxZoomLevelTextView.text.toString().toInt(),
                                customMapView,
                                requireContext(),
                            )
                                .downloadTile(true)
                        } else {
                            findNavController().navigate(R.id.action_downloadMapFragment_to_runtimeDialogFragment)
                        }*/
        }
    }

    private fun setTotalTilesByArea() {
        val mgr = CacheManager(customMapView)
        binding.tilesCountTextview.text =
            getString(com.bytecause.core.resources.R.string.tiles_count_textview).format(
                com.bytecause.util.string.StringUtil.formatNumberWithSpaces(
                    mgr.possibleTilesInArea(
                        customMapView.boundingBox,
                        binding.minZoomLevelTextView.text.toString().toInt(),
                        binding.maxZoomLevelTextView.text.toString().toInt(),
                    ),
                ),
            )
    }

    private fun setTileSource() {
        customMapView.setTileSource(
            when (val tileSource = mapSharedViewModel.tileSource.replayCache.lastOrNull()) {
                is TileSources.Raster.Default -> {
                    when (tileSource.id) {
                        DefaultTileSources.OPEN_TOPO.id -> TileSourceFactory.OpenTopo
                        DefaultTileSources.SATELLITE.id -> CustomTileSourceFactory.SAT
                        DefaultTileSources.MAPNIK.id -> TileSourceFactory.MAPNIK
                        else -> TileSourceFactory.MAPNIK
                    }
                }

                else -> TileSourceFactory.MAPNIK
            },
        )
    }

    private fun setSlidePrefs(currentZoom: Int) {
        val maxZoom = customMapView.tileProvider.tileSource.maximumZoomLevel

        val slider = binding.zoomRangeSlider
        val multipleZoomsSupported: Boolean = currentZoom <= maxZoom

        if (multipleZoomsSupported) {
            slider.apply {
                setValues(
                    currentZoom.toFloat(),
                    when {
                        slider.values.size == 1 -> maxZoom.toFloat()
                        slider.values[1] > maxZoom -> maxZoom.toFloat()
                        else -> slider.values[1]
                    },
                )
                valueFrom = 6f
                valueTo = maxZoom.toFloat()
                stepSize = 1f
            }
            customMapView.maxZoomLevel = slider.values[1].toDouble()
            binding.minZoomLevelTextView.text = currentZoom.toString()
            binding.maxZoomLevelTextView.text = slider.values[1].toInt().toString()
        }
    }

    private fun storageAvailable() {
        requireContext().storageAvailable().let {
            binding.availableStorage.apply {
                freeSpaceTextview.text =
                    resources.getString(
                        com.bytecause.core.resources.R.string.free,
                        it.entries.firstOrNull()?.key ?: "-"
                    )
                storageProgressBar.isIndeterminate = false
                storageProgressBar.progress = it.entries.firstOrNull()?.value ?: 0
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mapSharedViewModel.saveCameraPosition(
            CameraPosition.Builder()
                .zoom(customMapView.zoomLevelDouble)
                .target(
                    customMapView.mapCenter.run {
                        LatLng(latitude, longitude)
                    },
                )
                .build(),
        )
    }
}
