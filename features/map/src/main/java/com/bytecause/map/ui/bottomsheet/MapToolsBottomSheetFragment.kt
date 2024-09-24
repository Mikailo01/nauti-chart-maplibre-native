package com.bytecause.map.ui.bottomsheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.MapToolsBottomSheetFragmentBinding
import com.bytecause.map.util.navigateToCustomTileProviderNavigation
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.common.LastClick
import com.bytecause.util.delegates.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapToolsBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.map_tools_bottom_sheet_fragment) {

    private val binding by viewBinding(MapToolsBottomSheetFragmentBinding::bind)

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private val lastClick = LastClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        binding.customTileSourceImagebutton.setOnClickListener {
            if (!lastClick.lastClick()) return@setOnClickListener
            findNavController().navigateToCustomTileProviderNavigation()
        }

        binding.anchorageAlarmImageButton.setOnClickListener {
            if (!lastClick.lastClick()) return@setOnClickListener
            mapSharedViewModel.setShowAnchorageAlarmBottomSheet(true)
            dismiss()
        }

        binding.sallingRouteImageView.setOnClickListener {
            Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show()
        }
    }
}