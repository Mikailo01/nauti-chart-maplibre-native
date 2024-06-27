package com.bytecause.map.ui.bottomsheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.MapToolsBottomSheetFragmentBinding
import com.bytecause.map.util.navigateToCustomTileProviderNavigation
import com.bytecause.map.util.navigateToDownloadTilesNavigation
import com.bytecause.util.common.LastClick
import com.bytecause.util.delegates.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MapToolsBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.map_tools_bottom_sheet_fragment) {

    private val binding by viewBinding(MapToolsBottomSheetFragmentBinding::bind)

    private val lastClick = LastClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        binding.downloadMapImageButton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigateToDownloadTilesNavigation()
        }

        binding.customTileSourceImagebutton.setOnClickListener {
            if (!lastClick.lastClick(1000)) return@setOnClickListener
            findNavController().navigateToCustomTileProviderNavigation()
        }

        binding.sallingRouteImageView.setOnClickListener {
            Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show()
        }
    }
}