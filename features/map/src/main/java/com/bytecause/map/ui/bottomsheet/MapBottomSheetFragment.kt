package com.bytecause.map.ui.bottomsheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecause.domain.model.ArgsObjectTypeArray
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSourceTypes
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.MapBottomSheetBinding
import com.bytecause.map.ui.recyclerview.adapter.LayerParentAdapter
import com.bytecause.map.ui.recyclerview.interfaces.SelectLayerListener
import com.bytecause.map.ui.viewmodel.MapBottomSheetViewModel
import com.bytecause.presentation.components.views.dialog.ConfirmationDialog
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.file.FileUtil.offlineTilesDir
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


enum class LayerTypes {
    CUSTOM_ONLINE_RASTER_TILE_SOURCE,
    CUSTOM_OFFLINE_RASTER_TILE_SOURCE,
    CUSTOM_OFFLINE_VECTOR_TILE_SOURCE,
    ADDITIONAL_OVERLAY,
}

sealed interface MapBottomSheetResources {
    data class Custom(val name: String, val imageUrl: String? = null) : MapBottomSheetResources
}

@AndroidEntryPoint
class MapBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.map_bottom_sheet),
    SelectLayerListener,
    ConfirmationDialog.ConfirmationDialogListener {
    private val binding by viewBinding(MapBottomSheetBinding::bind)

    private val viewModel: MapBottomSheetViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var recyclerViewAdapter: LayerParentAdapter

    // when new element into recycler view is added, the scroll view will jump to the bottom, so this
    // listener scrolls back to the top
    private val onLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.mapBottomSheetNestedScrollView.fullScroll(
                View.FOCUS_UP
            )
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
                maxHeight = requireContext().resources.displayMetrics.heightPixels / 3
            }
        }

        binding.defaultTileProvider.apply {
            layerText.text = getString(com.bytecause.core.resources.R.string.default_tile_source)
            layerDrawable.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.terrain
                )
            )
            layersChildItemView.setOnClickListener {
                val tileSource =
                    (mapSharedViewModel.tileSource.replayCache.lastOrNull() as? TileSources.Raster.Default)

                if (tileSource == DefaultTileSources.MAPNIK) {
                    return@setOnClickListener
                } else {
                    DefaultTileSources.MAPNIK.let {
                        mapSharedViewModel.setTile(it)
                        viewModel.cacheSelectedTileSource(it.id.name)
                    }
                }
            }
        }

        binding.satelliteTileProvider.apply {
            layerText.text = getString(com.bytecause.core.resources.R.string.satellite)
            layerDrawable.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.satellite
                )
            )
            layersChildItemView.setOnClickListener {
                val tileSource =
                    (mapSharedViewModel.tileSource.replayCache.lastOrNull() as? TileSources.Raster.Default)

                if (tileSource == DefaultTileSources.SATELLITE) {
                    return@setOnClickListener
                } else {
                    DefaultTileSources.SATELLITE.let {
                        mapSharedViewModel.setTile(it)
                        viewModel.cacheSelectedTileSource(it.id.name)
                    }
                }
            }
        }

        binding.topographyTileProvider.apply {
            layerText.text = getString(com.bytecause.core.resources.R.string.topography)
            layerDrawable.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.topo_map
                )
            )
            layersChildItemView.setOnClickListener {
                val tileSource =
                    (mapSharedViewModel.tileSource.replayCache.lastOrNull() as? TileSources.Raster.Default)

                if (tileSource == DefaultTileSources.OPEN_TOPO) {
                    return@setOnClickListener
                } else {
                    DefaultTileSources.OPEN_TOPO.let {
                        mapSharedViewModel.setTile(it)
                        viewModel.cacheSelectedTileSource(it.id.name)
                    }
                }
            }
        }

        recyclerViewAdapter =
            LayerParentAdapter(
                emptyMap(),
                this@MapBottomSheetFragment,
            )

        recyclerView =
            binding.parentLayersRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = recyclerViewAdapter
            }

        recyclerView.addOnLayoutChangeListener(onLayoutChangeListener)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contentMapStateFlow.collect { newContent ->
                    newContent ?: return@collect

                    recyclerViewAdapter.submitList(newContent)
                }
            }
        }
    }

    override fun onItemViewClickListener(
        view: View,
        position: Int,
    ) {
        when (view.tag) {
            LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.customTileSources.firstOrNull()?.let { customTileSourceMap ->
                        customTileSourceMap[TileSourceTypes.RasterOnline]?.let { customRasterTileProviders ->
                            customRasterTileProviders[position].let { customRasterTileProvider ->
                                (customRasterTileProvider.type as CustomTileProviderType.Raster.Online).run {
                                    val customProvider =
                                        TileSources.Raster.Custom.Online(
                                            name = name,
                                            url = url,
                                            tileSize = tileSize,
                                            minZoom = minZoom.toFloat(),
                                            maxZoom = maxZoom.toFloat(),
                                        )

                                    customProvider.let {
                                        if (mapSharedViewModel.tileSource.replayCache.lastOrNull() == it) return@launch
                                        mapSharedViewModel.setTile(it)
                                        viewModel.cacheSelectedTileSource(it.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.customTileSources.firstOrNull()?.let { customTileSourceMap ->
                        customTileSourceMap[TileSourceTypes.RasterOffline]?.let { customRasterTileProviders ->
                            customRasterTileProviders[position].let { customRasterTileProvider ->
                                (customRasterTileProvider.type as CustomTileProviderType.Raster.Offline).run {

                                    val customProvider =
                                        TileSources.Raster.Custom.Offline(
                                            name = name,
                                            tileSize = tileSize,
                                            minZoom = minZoom.toFloat(),
                                            maxZoom = maxZoom.toFloat(),
                                            filePath = filePath
                                        )

                                    customProvider.let {
                                        if (mapSharedViewModel.tileSource.replayCache.lastOrNull() == it) return@launch
                                        mapSharedViewModel.setTile(it)
                                        viewModel.cacheSelectedTileSource(it.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.customTileSources.firstOrNull()?.let { customTileSourceMap ->
                        customTileSourceMap[TileSourceTypes.VectorOffline]?.let { customVectorTileProviders ->
                            customVectorTileProviders[position].let {
                                customVectorTileProviders[position].let { customVectorTileProvider ->
                                    (customVectorTileProvider.type as CustomTileProviderType.Vector.Offline).run {
                                        val customProvider =
                                            TileSources.Vector.Custom.Offline(
                                                name = name,
                                                minZoom = minZoom.toFloat(),
                                                maxZoom = maxZoom.toFloat(),
                                                filePath = filePath
                                            )

                                        customProvider.let {
                                            if (mapSharedViewModel.tileSource.replayCache.lastOrNull() == it) return@launch
                                            mapSharedViewModel.setTile(it)
                                            viewModel.cacheSelectedTileSource(it.name)
                                        }
                                    }
                                }
                            }
                        }

                        /* customVectorTileProviders[position].let { customVectorTileProvider ->
                             Log.d("idk", customVectorTileProvider.type.toString())

                             (customVectorTileProvider.type as CustomTileProviderType.Vector.Offline).run {
                                 val customProvider =
                                     TileSources.Vector.Custom.Offline(
                                         id = name,
                                         minZoom = minZoom.toFloat(),
                                         maxZoom = maxZoom.toFloat(),
                                         filePath = filePath
                                     )

                                 customProvider.let {
                                     if (mapSharedViewModel.tileSource.value == it) return@launch
                                     mapSharedViewModel.setTile(it)
                                     viewModel.cacheSelectedTileSource(it.id)
                                 }
                             }
                         }*/
                    }
                }
            }

            LayerTypes.ADDITIONAL_OVERLAY -> {
                // TODO("Additional overlays")
            }
        }
    }

    override fun onItemViewLongClickListener(
        view: View,
        parentPosition: Int,
        childPosition: Int
    ) {
        val dialog =
            ConfirmationDialog.newInstance(
                getString(com.bytecause.core.resources.R.string.delete_custom_tile_provider_prompt),
                null,
                ArgsObjectTypeArray.IntTypeArray(intArrayOf(parentPosition, childPosition)),
                "delete_tile_provider",
            )
        dialog.show(childFragmentManager, "ConfirmationDialog")
    }

    override fun onDialogPositiveClick(
        dialogId: String,
        additionalData: Any?,
    ) {
        (additionalData as? ArgsObjectTypeArray.IntTypeArray).let { position ->
            position ?: return

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.deleteCustomProvider(position.value[0], position.value[1]).firstOrNull()
                    ?.let { deletedProvider ->

                        // if deleted provider is offline provider, delete offline mbtiles
                        if (deletedProvider.first in listOf(
                                LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE,
                                LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE
                            )
                        ) {
                            deletedProvider.second?.let { providerName ->
                                viewModel.deleteOfflineTiles(
                                    requireContext().offlineTilesDir(),
                                    providerName
                                )
                            }
                        }

                        mapSharedViewModel.tileSource.replayCache.lastOrNull()?.let { tilesource ->
                            if (
                                (tilesource as? TileSources.Raster.Custom.Offline)?.name == deletedProvider.second ||
                                (tilesource as? TileSources.Raster.Custom.Online)?.name == deletedProvider.second ||
                                (tilesource as? TileSources.Vector.Custom.Offline)?.name == deletedProvider.second
                            ) {
                                // set default tile provider if the currently selected has been deleted
                                mapSharedViewModel.setTile(DefaultTileSources.MAPNIK)
                            }
                        }
                    }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.removeOnLayoutChangeListener(onLayoutChangeListener)
    }
}
