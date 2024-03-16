package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.TileSourceTypesDialogFragmentBinding
import com.bytecause.nautichart.tilesources.CustomTileSourceFactory
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class TileSourceTypes : DialogFragment() {

    private val binding by viewBinding(TileSourceTypesDialogFragmentBinding::inflate)

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mapSharedViewModel.tileSource.value) {
            TileSourceFactory.MAPNIK -> binding.stamenTileSource.isChecked = true
            CustomTileSourceFactory.SAT -> binding.satelliteTileSource.isChecked = true
            else -> binding.topographyTileSource.isChecked = true
        }

        binding.tileSourcesRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTileSource = when (checkedId) {
                R.id.stamen_tile_source -> TileSourceFactory.MAPNIK
                R.id.satellite_tile_source -> CustomTileSourceFactory.SAT
                R.id.topography_tile_source -> TileSourceFactory.OpenTopo

                else -> TileSourceFactory.MAPNIK
            }

            mapSharedViewModel.setTile(selectedTileSource)
            this.dismiss()
        }
    }
}