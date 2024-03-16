package com.bytecause.nautichart.ui.view.fragment.bottomsheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.MapToolsBottomSheetFragmentBinding
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MapToolsBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.map_tools_bottom_sheet_fragment) {

    private val binding by viewBinding(MapToolsBottomSheetFragmentBinding::bind)

    private val util = com.bytecause.nautichart.util.Util()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        binding.downloadMapImageButton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_mapToolsBottomSheetFragment_to_downloadMapFragment)
        }

        binding.customTileSourceImagebutton.setOnClickListener {
            if (!util.lastClick(1000)) return@setOnClickListener
            findNavController().navigate(R.id.action_mapToolsBottomSheetFragment_to_customTileSourceDialog)
        }
    }
}