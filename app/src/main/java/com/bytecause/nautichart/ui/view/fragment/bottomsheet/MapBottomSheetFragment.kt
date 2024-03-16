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
import com.bytecause.nautichart.interfaces.SelectLayerInterface
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.ui.adapter.LayerParentAdapter
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.MapBottomSheetViewModel
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

enum class LayerTypes {
    TILESOURCE,
    ADDITIONALOVERLAY
}

@AndroidEntryPoint
class MapBottomSheetFragment : BottomSheetDialogFragment(R.layout.map_bottom_sheet),
    SelectLayerInterface {

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
                    }
                }
            }
        }
    }

    override fun onItemViewClickListener(view: View, position: Int) {
        when (view.tag) {
            LayerTypes.TILESOURCE -> {
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
                }
            }

            LayerTypes.ADDITIONALOVERLAY -> {
                when (position) {
                    0 -> mapSharedViewModel.toggleGridOverlay()
                }
            }
        }
    }
}