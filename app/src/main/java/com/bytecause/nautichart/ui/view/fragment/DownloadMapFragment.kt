package com.bytecause.nautichart.ui.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.bytecause.nautichart.BuildConfig
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.cache.CacheDownloaderArchive
import com.bytecause.nautichart.databinding.DownloadTilesFragmentLayoutBinding
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.ui.util.storageAvailable
import com.bytecause.nautichart.ui.view.custom.CustomMapView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.util.StringUtil
import com.bytecause.nautichart.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import kotlin.properties.Delegates

@AndroidEntryPoint
class DownloadMapFragment : Fragment(R.layout.download_tiles_fragment_layout) {

    private val binding by viewBinding(DownloadTilesFragmentLayoutBinding::bind)

    private lateinit var customMapView: CustomMapView

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private var zoomLevel by Delegates.notNull<Double>()

    private val util = Util()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Configuration.getInstance().load(
            requireContext().applicationContext,
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zoomLevel = mapSharedViewModel.zoomLevel ?: 6.0

        customMapView = binding.mapWindow.apply {
            setTileSource(mapSharedViewModel.tileSource.value)
            maxZoomLevel = this.tileProvider.tileSource.maximumZoomLevel.toDouble()
            minZoomLevel = 6.0
            controller.setZoom(
                when {
                    this@DownloadMapFragment.zoomLevel < 6.0 -> 6.0
                    this@DownloadMapFragment.zoomLevel >= 6.0 && this@DownloadMapFragment.zoomLevel < maxZoomLevel -> this@DownloadMapFragment.zoomLevel
                    else -> this@DownloadMapFragment.zoomLevel - ((this@DownloadMapFragment.zoomLevel - maxZoomLevel))
                }
            )
            controller.setCenter(mapSharedViewModel.mapCenter)
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

        val zoomHandler = Handler(Looper.getMainLooper())
        customMapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                zoomHandler.removeCallbacksAndMessages(null)
                zoomHandler.postDelayed({
                    setSlidePrefs(customMapView.zoomLevelDouble.toInt())
                    mapSharedViewModel.setZoomLevel(customMapView.zoomLevelDouble)
                }, 100)
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                setTotalTilesByArea()
                return false
            }
        })

        storageAvailable()

        mapSharedViewModel.tileSource.observe(viewLifecycleOwner) {
            binding.currentTileSourceTextView.text =
                if (mapSharedViewModel.tileSource.value?.name() == TileSourceFactory.OpenTopo.name()) "Topography" else mapSharedViewModel.tileSource.value?.name()
            customMapView.setTileSource(mapSharedViewModel.tileSource.value)
            customMapView.invalidate()

            if (customMapView.zoomLevelDouble > customMapView.tileProvider.tileSource.maximumZoomLevel) {
                customMapView.apply {
                    controller.setZoom((customMapView.tileProvider.tileSource.maximumZoomLevel).toDouble())
                    maxZoomLevel = customMapView.tileProvider.tileSource.maximumZoomLevel.toDouble()
                }
                mapSharedViewModel.setZoomLevel(customMapView.zoomLevelDouble)
            }
            setSlidePrefs(customMapView.zoomLevelDouble.toInt())
            lifecycleScope.launch(Dispatchers.Main) {
                setTotalTilesByArea()
            }

        }

        binding.mapTileSourceType.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_downloadMapFragment_to_tileSourceTypes)
        }

        binding.toolbar.navBack.setOnClickListener {
            findNavController().popBackStack(R.id.map_dest, false)
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
            if (!util.lastClick(1000)) return@setOnClickListener

            if (mapSharedViewModel.tileSource.value != CustomTileSourceFactory.SAT
                && mapSharedViewModel.tileSource.value != TileSourceFactory.MAPNIK
            ) {
                CacheDownloaderArchive(
                    binding.minZoomLevelTextView.text.toString().toInt(),
                    binding.maxZoomLevelTextView.text.toString().toInt(),
                    customMapView,
                    requireContext()
                )
                    .downloadTile(true)
            } else {
                findNavController().navigate(R.id.action_downloadMapFragment_to_runtimeDialogFragment)
            }
        }
    }

    private fun setTotalTilesByArea() {
        val mgr = CacheManager(customMapView)
        binding.tilesCountTextview.text = getString(R.string.tiles_count_textview).format(
            StringUtil.formatNumberWithSpaces(
                mgr.possibleTilesInArea(
                    customMapView.boundingBox,
                    binding.minZoomLevelTextView.text.toString().toInt(),
                    binding.maxZoomLevelTextView.text.toString().toInt()
                )
            )
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
                    }
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
                    resources.getString(R.string.free, it.entries.firstOrNull()?.key ?: "-")
                storageProgressBar.isIndeterminate = false
                storageProgressBar.progress = it.entries.firstOrNull()?.value ?: 0
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mapSharedViewModel.setCenterPoint(customMapView.mapCenter)
    }
}