package com.bytecause.first_run.ui.bottomsheet

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.bytecause.features.first_run.R
import com.bytecause.features.first_run.databinding.SelectRegionBottomSheetLayoutBinding
import com.bytecause.first_run.ui.viewmodel.SelectRegionBottomSheetViewModel
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.common.LastClick
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SelectRegionBottomSheetDialog :
    BottomSheetDialogFragment(R.layout.select_region_bottom_sheet_layout) {

    private val binding by viewBinding(SelectRegionBottomSheetLayoutBinding::bind)

    private val viewModel: SelectRegionBottomSheetViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private val lastClick = LastClick()

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                mapSharedViewModel.permissionGranted(isGranted)
                findNavController().popBackStack()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        binding.grantLocationPermissionLinearLayout.setOnClickListener {
            if (!lastClick.lastClick(500)) return@setOnClickListener
            if (requireContext().isLocationPermissionGranted()) {
                findNavController().popBackStack()
            } else {
                activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.pickRegionLinearLayout.setOnClickListener {
            if (!lastClick.lastClick(500)) return@setOnClickListener
            viewModel.saveFirstRunFlag(false)

            // Navigation between features can be achieved using deep links only, because at compile time,
            // independent feature modules cannot see each other, so you can't use IDs to navigate to
            // destinations in other modules.
            val request = NavDeepLinkRequest.Builder
                .fromUri("nautichart://download_poi_dest_deep_link".toUri())
                .build()
            findNavController().navigate(request)
        }
    }
}