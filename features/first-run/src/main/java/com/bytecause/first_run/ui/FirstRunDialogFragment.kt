package com.bytecause.first_run.ui

import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.features.first_run.databinding.FirstRunDialogFragmentLayoutBinding
import com.bytecause.first_run.ui.viewmodel.FirstRunViewModel
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.context.isLocationPermissionGranted
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.extensions.TAG
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

    // Download is handled by this viewModel, that's why I scoped viewModel's lifecycle to the activity's
    // lifecycle this is temporary workaround to prevent from canceling download job when the user
    // dismiss FirstRunDialog, in future commits download will be handled by DownloadManager service.
    private val viewModel: FirstRunViewModel by activityViewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private val lastClick = com.bytecause.util.common.LastClick()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (requireContext().isLocationPermissionGranted()
            && mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() == null
        ) {
            showRegionLoading()
        }

        // If location permission is granted show progress bar.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.permissionGranted.collect {
                    if (it == null || !it) return@collect

                    showRegionLoading()
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
            viewModel.saveFirstRunFlag(false)
            findNavController().popBackStack()
        }

        binding.downloadButton.setOnClickListener {
            when (binding.downloadButton.text) {
                getString(R.string.download) -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.download_started),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Starts download job.
                    viewModel.getPoiResult()
                }

                getString(R.string.cancel) -> {
                    viewModel.cancelDownloadJob()
                    viewModel.resetUiState()
                    hideLoading()
                    toggleDownloadButtonState()
                }
            }
        }

        viewModel.region?.let {
            binding.regionNameTextView.apply {
                text = it
                alpha = 1f
            }
            toggleDownloadButtonState()
        } ?: viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.lastKnownPosition.take(1).collect { geoPoint ->
                    geoPoint ?: return@collect

                    showRegionLoading()

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                            ?.let {
                                withContext(Dispatchers.Main) {
                                    binding.regionNameTextView.apply {
                                        Log.d(TAG(), it.first().adminArea)
                                        it.first().adminArea.also {
                                            viewModel.setRegion(it)
                                            text = it
                                            alpha = 1f
                                        }
                                    }
                                    hideLoading()
                                    toggleDownloadButtonState()
                                }
                            }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateFlow.collect { uiState ->
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
                                showDownloadLoading()
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
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.region_download_success).format(uiState.items.firstOrNull()),
                                    Toast.LENGTH_SHORT
                                ).show()
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

    private fun showRegionLoading() {
        binding.loadingTextView.text = getString(R.string.fetching_location)
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingTextView.text = null
        binding.progressTextView.text = null
        binding.fetchingLocationProgressBarLinearLayout.visibility = View.GONE
    }

    private fun showDownloadLoading() {
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