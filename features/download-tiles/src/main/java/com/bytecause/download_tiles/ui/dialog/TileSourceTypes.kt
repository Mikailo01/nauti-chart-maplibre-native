package com.bytecause.download_tiles.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bytecause.domain.tilesources.DefaultTileSources
import com.bytecause.domain.tilesources.TileSources
import com.bytecause.features.download_tiles.R
import com.bytecause.features.download_tiles.databinding.TileSourceTypesDialogFragmentBinding
import com.bytecause.util.delegates.viewBinding

class TileSourceTypes : DialogFragment() {
    private val binding by viewBinding(
        TileSourceTypesDialogFragmentBinding::inflate
    )

    private val mapSharedViewModel: com.bytecause.presentation.viewmodels.MapSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        when (val tileSource = mapSharedViewModel.tileSource.value) {
            is TileSources.Raster.Default -> {
                when (tileSource) {
                    DefaultTileSources.MAPNIK -> binding.defaultTileSource.isChecked = true
                    DefaultTileSources.SATELLITE -> binding.satelliteTileSource.isChecked = true
                    DefaultTileSources.OPEN_TOPO -> binding.topographyTileSource.isChecked = true
                    else -> binding.defaultTileSource.isChecked = true
                }
            }

            else -> binding.defaultTileSource.isChecked = true
        }

        binding.tileSourcesRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTileSource =
                when (checkedId) {
                    R.id.default_tile_source -> DefaultTileSources.MAPNIK
                    R.id.satellite_tile_source -> DefaultTileSources.SATELLITE
                    R.id.topography_tile_source -> DefaultTileSources.OPEN_TOPO

                    else -> DefaultTileSources.MAPNIK
                }

            mapSharedViewModel.setTile(selectedTileSource)
            this.dismiss()
        }
    }
}
