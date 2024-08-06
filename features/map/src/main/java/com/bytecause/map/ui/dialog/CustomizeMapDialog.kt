package com.bytecause.map.ui.dialog

import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.CustomizeMapDialogFragmentLayoutBinding
import com.bytecause.map.ui.viewmodel.CustomizeMapViewModel
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.components.views.recyclerview.decorations.AdaptiveSpacingItemDecoration
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.bindings.RecyclerViewBindingInterface
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException

private const val SCROLL_VIEW_ALPHA = 200
private const val TOOLBAR_ALPHA = 168

@AndroidEntryPoint
class CustomizeMapDialog : DialogFragment() {
    private val binding by viewBinding(
        CustomizeMapDialogFragmentLayoutBinding::inflate
    )

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: CustomizeMapViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.MyCustomTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val bindingInterface =
            object : RecyclerViewBindingInterface<String> {
                override fun bindData(
                    item: String,
                    itemView: View,
                    itemPosition: Int,
                ) {
                    val textView: TextView = itemView.findViewById(R.id.poi_category_name_text_view)
                    val imageView: ImageView = itemView.findViewById(R.id.poi_category_image_view)

                    textView.text = item
                    imageView.setOnClickListener {
                        Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllDistinctCategories.collect { distinctCategoryList ->
                    if (distinctCategoryList.isEmpty()) {
                        binding.poiCategoriesRecyclerView.visibility = View.GONE
                        binding.noPoisDownloadedTextView.visibility = View.VISIBLE
                        return@collect
                    }

                    genericRecyclerViewAdapter =
                        GenericRecyclerViewAdapter(
                            distinctCategoryList,
                            R.layout.customize_map_recycler_view_item_view,
                            bindingInterface,
                        )

                    recyclerView =
                        binding.poiCategoriesRecyclerView.apply {
                            layoutManager =
                                GridLayoutManager(
                                    requireContext(),
                                    3,
                                    GridLayoutManager.HORIZONTAL,
                                    false,
                                )
                            adapter = genericRecyclerViewAdapter
                            addItemDecoration(AdaptiveSpacingItemDecoration(80, false))
                            setHasFixedSize(true)
                        }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipShowAll.apply {
            isChecked = mapSharedViewModel.showAllPois.value
            setOnClickListener {
                mapSharedViewModel.toggleShowAllPois()
            }
        }

        binding.chipAis.apply {
            isChecked = mapSharedViewModel.vesselLocationsVisible.value
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.fetchVessels()
                else mapSharedViewModel.toggleVesselLocations()
            }
        }

        binding.chipHarbours.apply {
            isChecked = mapSharedViewModel.harboursLocationsVisible.value
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.fetchHarbours()
                else mapSharedViewModel.toggleHarboursLocations()
            }
        }

        val layoutParams = binding.invisibleWindow.layoutParams
        layoutParams.height = resources.displayMetrics.heightPixels / 6
        binding.invisibleWindow.layoutParams = layoutParams

        binding.customizeMapScrollView.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.dialog_background,
                ),
                SCROLL_VIEW_ALPHA,
            ),
        )

        binding.toolbar.apply {
            toolbarAppBarLayout.setBackgroundColor(
                ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(
                        requireContext(),
                        com.bytecause.core.resources.R.color.dialog_background,
                    ),
                    TOOLBAR_ALPHA,
                ),
            )

            navBack.apply {
                setOnClickListener {
                    findNavController().popBackStack()
                }
            }
            destNameTextView.apply {
                text = findNavController().currentDestination?.label
            }
        }

        mapSharedViewModel.setIsCustomizeDialogVisible(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vesselsFetchingState.collect { state ->
                    state ?: return@collect
                    if (mapSharedViewModel.vesselLocationsVisible.value) return@collect

                    if (state.error != null) {
                        findNavController().popBackStack(
                            R.id.customizeMapDialog,
                            false,
                        )
                    }

                    when (val exception = state.error) {
                        is IOException -> {
                            if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(
                                        if (exception is ConnectException) com.bytecause.core.resources.R.string.service_unavailable
                                        else com.bytecause.core.resources.R.string.no_network_available
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                mapSharedViewModel.toggleVesselLocations()
                                binding.chipAis.isChecked = false
                            }
                        }

                        null -> {
                            if (state.isLoading) {
                                if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                    val action =
                                        CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                            getString(com.bytecause.core.resources.R.string.loading_vessels_text),
                                        )
                                    findNavController().navigate(action)
                                }
                            } else {
                                mapSharedViewModel.toggleVesselLocations()
                                if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                    findNavController().popBackStack(R.id.customizeMapDialog, false)
                                }
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.something_went_wrong),
                                Toast.LENGTH_SHORT,
                            ).show()
                            mapSharedViewModel.toggleVesselLocations()
                            binding.chipAis.isChecked = false
                        }

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.harboursFetchingState.collect { state ->
                    state ?: return@collect
                    if (mapSharedViewModel.harboursLocationsVisible.value) return@collect

                    if (state.error != null) {
                        findNavController().popBackStack(
                            R.id.customizeMapDialog,
                            false,
                        )
                    }

                    when (val exception = state.error) {
                        is IOException -> {
                            if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(
                                        if (exception is ConnectException) com.bytecause.core.resources.R.string.service_unavailable
                                        else com.bytecause.core.resources.R.string.no_network_available
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                mapSharedViewModel.toggleHarboursLocations()
                                binding.chipHarbours.isChecked = false
                            }
                        }

                        null -> {
                            if (state.isLoading) {
                                if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                    val action =
                                        CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                            "Fetching harbours..."
                                        )
                                    findNavController().navigate(action)
                                }
                            } else {
                                mapSharedViewModel.toggleHarboursLocations()
                                if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                    findNavController().popBackStack(R.id.customizeMapDialog, false)
                                }
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.something_went_wrong),
                                Toast.LENGTH_SHORT,
                            ).show()
                            mapSharedViewModel.toggleHarboursLocations()
                            binding.chipHarbours.isChecked = false
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Apply the fullscreen dialog style
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.transparent,
                ),
            ),
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mapSharedViewModel.setIsCustomizeDialogVisible(false)
    }
}