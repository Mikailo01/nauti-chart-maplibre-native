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
import com.bytecause.data.services.Actions
import com.bytecause.data.services.REGION_ID_PARAM
import com.bytecause.data.services.REGION_NAME_PARAM
import com.bytecause.data.services.REGION_POI_DOWNLOAD_SERVICE_CLASS
import com.bytecause.features.first_run.databinding.FirstRunDialogFragmentLayoutBinding
import com.bytecause.first_run.ui.viewmodel.FirstRunSharedViewModel
import com.bytecause.first_run.ui.viewmodel.FirstRunViewModel
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import java.io.IOException
import java.net.ConnectException
import java.util.Locale

enum class ActionButtonState {
    Download,
    Cancel,
    TryAgain
}

@AndroidEntryPoint
class FirstRunDialogFragment : DialogFragment() {

    private val binding by viewBinding(
        FirstRunDialogFragmentLayoutBinding::inflate
    )

    private val viewModel: FirstRunViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val sharedViewModel: FirstRunSharedViewModel by activityViewModels()

    private val lastClick = com.bytecause.util.common.LastClick()

    private lateinit var geocoder: Geocoder

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
        }.takeIf { viewModel.region != null }?.cancel() // cancel scope if region is already fetched

        this.isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geocoder = Geocoder(requireContext(), Locale.getDefault())

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

        binding.actionButton.setOnClickListener {
            when (binding.actionButton.tag) {
                ActionButtonState.Download -> {
                    viewModel.region?.let { region ->

                        // Start service
                        Intent().apply {
                            setClassName(requireActivity(), REGION_POI_DOWNLOAD_SERVICE_CLASS)
                            setAction(Actions.START.toString())
                            putExtra(
                                REGION_ID_PARAM,
                                region.regionId
                            )
                            putExtra(
                               REGION_NAME_PARAM,
                                region.regionName
                            )
                            requireActivity().startService(this)
                        }

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.saveFirstRunFlag(false)
                        }
                    }
                }

                ActionButtonState.Cancel -> {
                    // Stop service
                    Intent().apply {
                        setClassName(requireActivity(), REGION_POI_DOWNLOAD_SERVICE_CLASS)
                        setAction(Actions.STOP.toString())
                        requireActivity().startService(this)
                    }

                    hideLoading()
                    toggleDownloadButtonState(ActionButtonState.Download)
                }

                ActionButtonState.TryAgain -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        mapSharedViewModel.lastKnownPosition.replayCache.firstOrNull()
                            ?.let { geoPoint ->
                                getRegionsFromGeoPoint(geoPoint)
                            }
                    }
                }
            }
        }

        viewModel.region?.regionName?.let {
            binding.regionNameTextView.apply {
                text = it
                alpha = 1f
            }
            toggleDownloadButtonState(ActionButtonState.Download)
        } ?: viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.lastKnownPosition.collect { geoPoint ->
                    geoPoint ?: return@collect
                    // regions already fetched, return collect
                    if (viewModel.downloadRegionsUiState.value != null) return@collect

                    showFetchingLocationLoading()

                    getRegionsFromGeoPoint(geoPoint)
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

                            toggleDownloadButtonState(ActionButtonState.TryAgain)
                        }

                        null -> {
                            if (uiState.loading.isLoading) {
                                showDownloadRegionsLoading()
                                toggleDownloadButtonState(null)
                            } else {
                                hideLoading()

                                sharedViewModel.setRegions(uiState.items.sortedBy {
                                    it.names["name:${Locale.getDefault().language}"]
                                        ?: it.names["name:en"]
                                        ?: it.names["name"]
                                })
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

                            toggleDownloadButtonState(ActionButtonState.TryAgain)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadPoiUiState.collect { uiState ->
                    uiState ?: run {
                        if (binding.actionButton.tag == ActionButtonState.Cancel) {
                            toggleDownloadButtonState(ActionButtonState.Download)
                            hideLoading()
                        }
                        return@collect
                    }

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

                            toggleDownloadButtonState(ActionButtonState.Download)
                        }

                        null -> {
                            if (uiState.loading.isLoading) {
                                showDownloadPoiLoading()
                                toggleDownloadButtonState(ActionButtonState.Cancel)

                                uiState.loading.progress?.let { progress ->
                                    binding.loadingTextView.text =
                                        getString(R.string.processed_count).format(progress)
                                }
                            } else {
                                // download successful, hide fullscreen dialog
                                findNavController().popBackStack()
                            }
                        }

                        else -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()

                            toggleDownloadButtonState(ActionButtonState.Download)
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

                    toggleDownloadButtonState(ActionButtonState.Download)
                }
            }
        }
    }

    private fun toggleDownloadButtonState(actionState: ActionButtonState?) {
        when (actionState) {
            ActionButtonState.Download -> {
                binding.actionButton.apply {
                    text = getString(R.string.download)
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onSecondaryContainer
                        )
                    )
                    tag = ActionButtonState.Download
                    visibility = View.VISIBLE
                }
            }

            ActionButtonState.Cancel -> {
                binding.actionButton.apply {
                    text = getString(R.string.cancel)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error))
                    tag = ActionButtonState.Cancel
                    visibility = View.VISIBLE
                }
            }

            ActionButtonState.TryAgain -> {
                binding.actionButton.apply {
                    text = getString(R.string.try_again)
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_onSecondaryContainer
                        )
                    )
                    tag = ActionButtonState.TryAgain
                    visibility = View.VISIBLE
                }
            }

            null -> {
                binding.actionButton.apply {
                    text = null
                    tag = null
                    visibility = View.GONE
                }
            }
        }
    }

    private fun showFetchingLocationLoading() {
        binding.loadingTextView.text = getString(R.string.fetching_location)
        binding.horizontalProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingTextView.text = null
        binding.progressTextView.text = null
        binding.horizontalProgressBarLinearLayout.visibility = View.GONE
    }

    private fun showDownloadRegionsLoading() {
        binding.loadingTextView.text = getString(R.string.downloading_country_regions)
        binding.horizontalProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private fun showDownloadPoiLoading() {
        binding.loadingTextView.text = getString(R.string.downloading)
        binding.horizontalProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private suspend fun getRegionsFromGeoPoint(geoPoint: LatLng) {
        withContext(Dispatchers.IO) {
            geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                ?.let {
                    viewModel.getRegions(
                        isoCode = it.first().countryCode
                    )
                }
        }
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