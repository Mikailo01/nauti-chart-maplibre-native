package com.bytecause.nautichart.ui.view.fragment.dialog

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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.FirstRunDialogFragmentLayoutBinding
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.ui.util.isLocationPermissionGranted
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.FirstRunViewModel
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.util.TAG
import com.bytecause.nautichart.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class FirstRunDialogFragment : DialogFragment() {

    private val binding by viewBinding(FirstRunDialogFragmentLayoutBinding::inflate)

    // Download is handled by this viewModel, that's why I scoped viewModel's lifecycle to the activity's
    // lifecycle this is temporary workaround to prevent from canceling download job when the user
    // dismiss FirstRunDialog, in future commits download will be handled by DownloadManager service.
    private val viewModel: FirstRunViewModel by activityViewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (requireContext().isLocationPermissionGranted()
            && mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() == null
        ) {
            showProgressBar(isVisible = true, isDownloading = false)
        }

        // If location permission is granted show progress bar.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSharedViewModel.permissionGranted.collect {
                    if (it == null || !it) return@collect

                    showProgressBar(isVisible = true, isDownloading = false)
                }
            }
        }


        this.isCancelable = false
        return binding.root
    }

    //private val geocodeListener = GeocodeListener {  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectRegionImageView.setOnClickListener {
            if (!Util.lastClick(500)) return@setOnClickListener
            findNavController().navigate(R.id.action_firstRunDialogFragment_to_selectRegionBottomSheetDialog)
        }

        binding.skipTextView.setOnClickListener {
            if (!Util.lastClick(500)) return@setOnClickListener
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
                    viewModel.getPoiResult(
                        binding.regionNameTextView.text.toString()
                    )
                }

                getString(R.string.cancel) -> {
                    viewModel.cancelDownloadJob()
                    viewModel.resetUiState()
                    showProgressBar(isVisible = false, isDownloading = false)
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

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)?.let {
                        withContext(Dispatchers.Main) {
                            binding.regionNameTextView.apply {
                                Log.d(TAG(), it.first().adminArea)
                                it.first().adminArea.also {
                                    viewModel.setRegion(it)
                                    text = it
                                    alpha = 1f
                                }
                            }
                            showProgressBar(isVisible = false, isDownloading = false)
                            toggleDownloadButtonState()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateFlow.collect { uiState ->
                    uiState ?: return@collect

                    when (uiState.error) {
                        UiState.Error.NetworkError -> {
                            showProgressBar(isVisible = false, isDownloading = false)
                            toggleDownloadButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.no_network_available),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        UiState.Error.ServiceUnavailable -> {
                            showProgressBar(isVisible = false, isDownloading = false)
                            toggleDownloadButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.service_unavailable),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        UiState.Error.Other -> {
                            showProgressBar(isVisible = false, isDownloading = false)
                            toggleDownloadButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        null -> {
                            if (uiState.isLoading) {
                                showProgressBar(isVisible = true, isDownloading = true)
                                toggleDownloadButtonState()
                            } else {
                                showProgressBar(isVisible = false, isDownloading = false)
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

    private fun showProgressBar(isVisible: Boolean, isDownloading: Boolean) {
        //if (binding.regionNameTextView.text.isNotEmpty()) return
        binding.loadingTextView.apply {
            text = if (isDownloading) getString(R.string.downloading)
            else getString(R.string.fetching_location)
        }

        binding.fetchingLocationProgressBarLinearLayout.visibility =
            if (isVisible) View.VISIBLE else View.GONE
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
                    R.color.dialog_background
                )
            )
        )
    }
}