package com.bytecause.first_run.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.data.services.RegionPoiDownloadService
import com.bytecause.features.first_run.databinding.FirstRunDialogFragmentLayoutBinding
import com.bytecause.first_run.ui.viewmodel.FirstRunSharedViewModel
import com.bytecause.first_run.ui.viewmodel.FirstRunViewModel
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.util.Locale

@AndroidEntryPoint
class FirstRunDialogFragment : DialogFragment() {

    private val binding by viewBinding(
        FirstRunDialogFragmentLayoutBinding::inflate
    )

    private val viewModel: FirstRunViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val sharedViewModel: FirstRunSharedViewModel by activityViewModels()

    private val lastClick = com.bytecause.util.common.LastClick()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (requireContext().isLocationPermissionGranted()
            && mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() == null
        ) {
            showFetchingLocationLoading()
        }

        // If location permission is granted show progress bar.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.permissionGranted.collect {
                    if (it == null || !it) return@collect

                    showFetchingLocationLoading()
                }
            }
        }

        this.isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectRegionImageView.setOnClickListener {
            if (!lastClick.lastClick(500)) return@setOnClickListener
            findNavController().navigate(com.bytecause.features.first_run.R.id.action_firstRunDialogFragment_to_selectRegionBottomSheetDialog)
        }

        binding.skipTextView.setOnClickListener {
            if (!lastClick.lastClick(500)) return@setOnClickListener
            // Saves flag which indicates if app is started for the first time.
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveFirstRunFlag(false)
                findNavController().popBackStack()
            }
        }

        binding.downloadButton.setOnClickListener {
            when (binding.downloadButton.text) {
                getString(R.string.download) -> {
                    viewModel.region?.let { region ->
                        // Start service
                        Intent(
                            activity,
                            RegionPoiDownloadService::class.java
                        ).also {
                            it.setAction(RegionPoiDownloadService.Actions.START.toString())
                            it.putExtra(
                                RegionPoiDownloadService.REGION_ID_PARAM,
                                region.regionId
                            )
                            it.putExtra(
                                RegionPoiDownloadService.REGION_NAME_PARAM,
                                region.regionName
                            )
                            requireActivity().startService(it)
                        }

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.download_will_be_finished_in_the_background_),
                            Toast.LENGTH_SHORT
                        ).show()

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.saveFirstRunFlag(false)
                            findNavController().popBackStack()
                        }
                    }
                }

                getString(R.string.cancel) -> {
                    viewModel.cancelDownloadJob()
                    viewModel.resetUiState()
                    hideLoading()
                    toggleDownloadButtonState()
                }
            }
        }

        viewModel.region?.regionName?.let {
            binding.regionNameTextView.apply {
                text = it
                alpha = 1f
            }
            toggleDownloadButtonState()
        } ?: viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.lastKnownPosition.take(1).collect { geoPoint ->
                    geoPoint ?: return@collect
                    // regions already fetched, return collect
                    if (viewModel.downloadRegionsUiState.value != null) return@collect

                    showFetchingLocationLoading()

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                            ?.let {
                                viewModel.getRegions(
                                    isoCode = it.first().countryCode
                                )
                            }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadRegionsUiState.collect { uiState ->
                    uiState ?: return@collect
                    if (sharedViewModel.regionsSharedFlow.value.isNotEmpty()) return@collect

                    when (val exception = uiState.error) {
                        is IOException -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    if (exception is ConnectException) R.string.service_unavailable
                                    else R.string.no_network_available
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        null -> {
                            if (uiState.loading.isLoading) {
                                showDownloadRegionsLoading()
                            } else {
                                hideLoading()

                                sharedViewModel.saveRegions(uiState.items)
                                findNavController().navigate(com.bytecause.features.first_run.R.id.selectRegionComposedDialog)
                            }
                        }

                        else -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadPoiUiState.collect { uiState ->
                    uiState ?: return@collect

                    when (val exception = uiState.error) {
                        is IOException -> {
                            hideLoading()
                            toggleDownloadButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    if (exception is ConnectException) R.string.service_unavailable
                                    else R.string.no_network_available
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        null -> {
                            if (uiState.loading.isLoading) {
                                showDownloadPoiLoading()
                                toggleDownloadButtonState()
                                uiState.loading.progress?.let {
                                    binding.loadingTextView.text = getString(R.string.processing)
                                    binding.progressTextView.text =
                                        getString(R.string.processed_count).format(it)
                                }
                            } else {
                                hideLoading()
                                toggleDownloadButtonState()
                                viewModel.saveFirstRunFlag(false)
                                dismiss()
                            }
                        }

                        else -> {
                            hideLoading()
                            toggleDownloadButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedRegion.collect { region ->
                    region ?: return@collect

                    region.run {
                        viewModel.setRegion(
                            Region(
                                regionName = names["name"] ?: "",
                                countryId = countryId,
                                regionId = id
                            )
                        )
                    }

                    val lang = Locale.getDefault().language

                    val regionName = region.names["name:$lang"]
                        ?: region.names["name:en"]
                        ?: region.names["name"]

                    binding.regionNameTextView.apply {
                        text = regionName
                        alpha = 1f
                    }

                    toggleDownloadButtonState()
                }
            }
        }
    }

    private fun toggleDownloadButtonState() {
        if (binding.fetchingLocationProgressBarLinearLayout.visibility == View.VISIBLE) {
            binding.downloadButton.apply {
                text = getString(R.string.cancel)
                visibility = View.VISIBLE
            }
        } else {
            binding.downloadButton.apply {
                text = getString(R.string.download)
                visibility = View.VISIBLE
            }
        }
    }

    private fun showFetchingLocationLoading() {
        binding.loadingTextView.text = getString(R.string.fetching_location)
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingTextView.text = null
        binding.progressTextView.text = null
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.GONE
    }

    private fun showDownloadRegionsLoading() {
        binding.loadingTextView.text = getString(R.string.downloading_country_regions)
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private fun showDownloadPoiLoading() {
        binding.loadingTextView.text = getString(R.string.downloading)
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        // Apply the fullscreen dialog style
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_surface
                )
            )
        )
    }
}