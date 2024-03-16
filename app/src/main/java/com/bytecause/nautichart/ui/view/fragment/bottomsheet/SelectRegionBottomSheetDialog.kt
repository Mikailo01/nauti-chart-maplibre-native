package com.bytecause.nautichart.ui.view.fragment.bottomsheet

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.SelectRegionBottomSheetLayoutBinding
import com.bytecause.nautichart.ui.util.isLocationPermissionGranted
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.ui.viewmodels.SelectRegionBottomSheetViewModel
import com.bytecause.nautichart.util.Util
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SelectRegionBottomSheetDialog :
    BottomSheetDialogFragment(R.layout.select_region_bottom_sheet_layout) {

    private val binding by viewBinding(SelectRegionBottomSheetLayoutBinding::bind)

    private val viewModel: SelectRegionBottomSheetViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private val util = Util()

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                mapSharedViewModel.permissionGranted(isGranted)
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "locationGranted",
                    isGranted
                )
                findNavController().popBackStack()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.grantLocationPermissionLinearLayout.setOnClickListener {
            if (!util.lastClick(500)) return@setOnClickListener
            if (requireContext().isLocationPermissionGranted()) {
                findNavController().popBackStack()
            } else {
                activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.pickRegionLinearLayout.setOnClickListener {
            if (!util.lastClick(500)) return@setOnClickListener
            viewModel.saveFirstRunFlag(false)
            findNavController().navigate(R.id.action_selectRegionBottomSheetDialog_to_download_poi_dest)
        }
    }
}