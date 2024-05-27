package com.bytecause.nautichart.ui.view.fragment.bottomsheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.MapBottomSheetBinding
import com.bytecause.nautichart.domain.model.ArgsObjectTypeArray
import com.bytecause.nautichart.domain.model.CustomTileProviderType
import com.bytecause.nautichart.interfaces.SelectLayerInterface
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.tilesources.TileProviderUrlParametersSchema
import com.bytecause.nautichart.tilesources.YXTileSource
import com.bytecause.nautichart.ui.adapter.LayerParentAdapter
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.view.fragment.dialog.ConfirmationDialog
import com.bytecause.nautichart.ui.viewmodels.MapBottomSheetViewModel
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource

enum class LayerTypes {
    TILE_SOURCE,
    ADDITIONAL_OVERLAY
}

sealed interface MapBottomSheetResources {

    data object Default : MapBottomSheetResources
    data object Satellite : MapBottomSheetResources
    data object Topo : MapBottomSheetResources
    data object Grid : MapBottomSheetResources
    data class Custom(val name: String) : MapBottomSheetResources

}

// TODO("Image rendering slows down opening of this fragment.")
@AndroidEntryPoint
class MapBottomSheetFragment : BottomSheetDialogFragment(R.layout.map_bottom_sheet),
    SelectLayerInterface, ConfirmationDialog.ConfirmationDialogListener {

    private val binding by viewBinding(MapBottomSheetBinding::bind)

    private val viewModel: MapBottomSheetViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var recyclerViewAdapter: LayerParentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contentMapStateFlow.collect { newContent ->

                    if (!::recyclerView.isInitialized) {
                        recyclerViewAdapter = LayerParentAdapter(
                            newContent, this@MapBottomSheetFragment
                        )

                        recyclerView = binding.parentLayersRecyclerView.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = recyclerViewAdapter
                        }
                    } else {
                        recyclerViewAdapter.submitList(newContent)
                    }
                }
            }
        }
    }

    override fun onItemViewClickListener(view: View, position: Int) {
        when (view.tag) {
            LayerTypes.TILE_SOURCE -> {
                when (position) {
                    0 -> {
                        if (mapSharedViewModel.tileSource.value == TileSourceFactory.MAPNIK) {
                            return
                        } else {
                            TileSourceFactory.MAPNIK.let {
                                mapSharedViewModel.setTile(it)
                                viewModel.cacheSelectedTileSource(it.name())
                            }
                        }
                    }

                    1 -> {
                        if (mapSharedViewModel.tileSource.value == CustomTileSourceFactory.SAT) {
                            return
                        } else {
                            CustomTileSourceFactory.SAT.let {
                                mapSharedViewModel.setTile(it)
                                viewModel.cacheSelectedTileSource(it.name())
                            }
                        }
                    }

                    2 -> {
                        if (mapSharedViewModel.tileSource.value == TileSourceFactory.OpenTopo) {
                            return
                        } else {
                            TileSourceFactory.OpenTopo.let {
                                mapSharedViewModel.setTile(it)
                                viewModel.cacheSelectedTileSource(it.name())
                            }
                        }
                    }

                    else -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.customTileSources.firstOrNull()?.let { customTileProviders ->
                                customTileProviders[position - 3].let { customTileProvider ->
                                    when (customTileProvider.type) {
                                        is CustomTileProviderType.Online -> {
                                            val customProvider: OnlineTileSourceBase? =
                                                when (customTileProvider.type.schema) {
                                                    TileProviderUrlParametersSchema.YX.name -> {
                                                        YXTileSource(
                                                            aName = customTileProvider.type.name,
                                                            minZoom = customTileProvider.type.minZoom,
                                                            maxZoom = customTileProvider.type.maxZoom,
                                                            tileSize = customTileProvider.type.tileSize,
                                                            filenameEnding = customTileProvider.type.tileFileFormat,
                                                            url = arrayOf(customTileProvider.type.url),
                                                            copyright = ""
                                                        )
                                                    }

                                                    TileProviderUrlParametersSchema.XY.name -> {
                                                        XYTileSource(
                                                            customTileProvider.type.name,
                                                            customTileProvider.type.minZoom,
                                                            customTileProvider.type.maxZoom,
                                                            customTileProvider.type.tileSize,
                                                            customTileProvider.type.tileFileFormat,
                                                            arrayOf(
                                                                customTileProvider.type.url
                                                            )
                                                        )
                                                    }

                                                    else -> {
                                                        null
                                                    }
                                                }
                                            customProvider?.let {
                                                mapSharedViewModel.setTile(it)
                                                viewModel.cacheSelectedTileSource(it.name())
                                            }
                                        }

                                        is CustomTileProviderType.Offline -> {
                                            val customProvider: OnlineTileSourceBase = XYTileSource(
                                                customTileProvider.type.name,
                                                customTileProvider.type.minZoom,
                                                customTileProvider.type.maxZoom,
                                                customTileProvider.type.tileSize,
                                                "",
                                                emptyArray()
                                            )

                                            mapSharedViewModel.setTile(customProvider)
                                            viewModel.cacheSelectedTileSource(customProvider.name())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LayerTypes.ADDITIONAL_OVERLAY -> {
                when (position) {
                    0 -> mapSharedViewModel.toggleGridOverlay()
                }
            }
        }
    }

    override fun onItemViewLongClickListener(view: View, position: Int) {
        val dialog = ConfirmationDialog.newInstance(
            getString(R.string.delete_custom_tile_provider_prompt),
            null,
            ArgsObjectTypeArray.IntType(position),
            "delete_tile_provider"
        )
        dialog.show(childFragmentManager, "ConfirmationDialog")
    }

    override fun onDialogPositiveClick(dialogId: String, additionalData: Any?) {
        (additionalData as? Int).let { position ->
            position ?: return

            viewModel.deleteCustomProvider(position)
            mapSharedViewModel.setTile(TileSourceFactory.MAPNIK)
        }
    }
}