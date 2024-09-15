package com.bytecause.map.ui.dialog

import android.animation.ValueAnimator
import android.content.DialogInterface
import android.graphics.PorterDuff
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
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.CustomizeMapDialogFragmentLayoutBinding
import com.bytecause.map.ui.model.PoiCategory
import com.bytecause.map.ui.viewmodel.CustomizeMapViewModel
import com.bytecause.map.ui.viewmodel.LoadingDialogSharedViewModel
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.components.views.recyclerview.decorations.AdaptiveSpacingItemDecoration
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.bindings.RecyclerViewBindingInterface
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.poi.PoiUtil.getDrawableForPoiCategory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException


private const val SCROLL_VIEW_ALPHA = 200
private const val TOOLBAR_ALPHA = 168
private const val ITEM_SELECTED_ANIMATION_DURATION = 500L

@AndroidEntryPoint
class CustomizeMapDialog : DialogFragment() {
    private val binding by viewBinding(
        CustomizeMapDialogFragmentLayoutBinding::inflate
    )

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: CustomizeMapViewModel by viewModels()
    private val loadingDialogSharedViewModel: LoadingDialogSharedViewModel by activityViewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<PoiCategory>

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
            object : RecyclerViewBindingInterface<PoiCategory> {
                override fun bindData(
                    item: PoiCategory,
                    itemView: View,
                    itemPosition: Int,
                ) {
                    val textView: TextView = itemView.findViewById(R.id.poi_category_name_text_view)
                    val outerImageView: ImageView =
                        itemView.findViewById(R.id.poi_category_outer_image_view)
                    val innerImageView: ImageView =
                        itemView.findViewById(R.id.poi_category_inner_image_view)

                    fun applyColorAnimation(isSelected: Boolean) {
                        if (isSelected) {
                            val startColor = ContextCompat.getColor(
                                requireContext(),
                                com.bytecause.core.resources.R.color.gray
                            )
                            val endColor = ContextCompat.getColor(
                                requireContext(),
                                com.bytecause.core.resources.R.color.md_theme_primary
                            )

                            // Create a ValueAnimator that animates between the start and end colors.
                            val colorAnimation = ValueAnimator.ofArgb(startColor, endColor).apply {
                                duration = ITEM_SELECTED_ANIMATION_DURATION
                                addUpdateListener { animator ->
                                    val color = animator.animatedValue as Int
                                    outerImageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                                }
                            }
                            colorAnimation.start()

                            innerImageView.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    com.bytecause.core.resources.R.color.md_theme_onPrimary
                                )
                            )
                        } else {
                            val color = ContextCompat.getColor(
                                requireContext(),
                                com.bytecause.core.resources.R.color.gray
                            )

                            outerImageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                            innerImageView.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    com.bytecause.core.resources.R.color.md_theme_onSurface
                                )
                            )
                        }
                    }

                    applyColorAnimation(item.isSelected)

                    val name = getString(item.nameRes)

                    textView.apply {
                        text = name
                        isSelected = true
                    }

                    getDrawableForPoiCategory(name, requireContext())?.let {
                        innerImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                it
                            )
                        )
                    } ?: innerImageView.setImageDrawable(null)

                    outerImageView.setOnClickListener {
                        if (item.isSelected) viewModel.removeSelectedCategory(item)
                        else viewModel.setSelectedCategory(item)
                    }
                }
            }

        genericRecyclerViewAdapter =
            GenericRecyclerViewAdapter(
                emptyList(),
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllDistinctCategories.collect { distinctCategoryList ->
                    if (distinctCategoryList.isEmpty()) {
                        binding.poiCategoriesRecyclerView.visibility = View.GONE
                        binding.noPoisDownloadedTextView.visibility = View.VISIBLE
                    } else {
                        binding.poiCategoriesRecyclerView.visibility = View.VISIBLE
                        binding.noPoisDownloadedTextView.visibility = View.GONE
                    }

                    binding.chipShowAll.isChecked =
                        distinctCategoryList.takeIf { it.isNotEmpty() }?.all { poiCategory ->
                            poiCategory.isSelected
                        } ?: false

                    genericRecyclerViewAdapter.updateContent(
                        sortCategoriesBySelectionAndName(distinctCategoryList)
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAisActivated.collect { isActivated ->
                    binding.chipAis.isChecked = isActivated
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.areHarboursVisible.collect { isActivated ->
                    binding.chipHarbours.isChecked = isActivated
                }
            }
        }

        return binding.root
    }

    private fun sortCategoriesBySelectionAndName(categories: List<PoiCategory>): List<PoiCategory> {
        return categories.sortedWith(
            // Sort by isSelected first (descending)
            compareByDescending<PoiCategory> { poiCategory ->
                poiCategory.isSelected
            }
                // Then sort by name (ascending)
                .thenBy { poiCategory ->
                    getString(poiCategory.nameRes)
                }
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipShowAll.apply {
            setOnClickListener {
                if (isChecked) viewModel.selectAllCategories()
                else viewModel.unselectAllCategories()
            }
        }

        binding.chipAis.apply {
            setOnClickListener {
                if (isChecked) {
                    viewModel.toggleAisActivation()
                    viewModel.fetchVessels()
                } else {
                    viewModel.toggleAisActivation()
                }
            }
        }

        binding.chipHarbours.apply {
            setOnClickListener {
                if (isChecked) {
                    viewModel.toggleHarboursVisible()
                    viewModel.fetchHarbours()
                } else {
                    viewModel.toggleHarboursVisible()
                }
            }
        }

        val layoutParams = binding.invisibleWindow.layoutParams
        layoutParams.height = resources.displayMetrics.heightPixels / 6
        binding.invisibleWindow.layoutParams = layoutParams

        binding.customizeMapScrollView.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.md_theme_surface,
                ),
                SCROLL_VIEW_ALPHA,
            ),
        )

        binding.toolbar.apply {
            val contentColor = ContextCompat.getColor(
                requireContext(),
                com.bytecause.core.resources.R.color.md_theme_onSurface,
            )

            toolbarAppBarLayout.setBackgroundColor(
                ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(
                        requireContext(),
                        com.bytecause.core.resources.R.color.md_theme_surface,
                    ),
                    TOOLBAR_ALPHA,
                ),
            )

            navBack.apply {
                setColorFilter(contentColor)
                setOnClickListener {
                    findNavController().popBackStack()
                }
            }
            destNameTextView.apply {
                setTextColor(contentColor)
                text = findNavController().currentDestination?.label
            }
        }

        mapSharedViewModel.setIsCustomizeDialogVisible(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vesselsFetchingState.collect { state ->
                    state ?: return@collect
                    if (!binding.chipAis.isChecked) return@collect

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
                            }
                            if (viewModel.isAisActivated.value) viewModel.toggleAisActivation()
                        }

                        null -> {
                            if (state.loading.isLoading) {
                                if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                    val action =
                                        CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                            getString(com.bytecause.core.resources.R.string.loading_vessels_text),
                                        )
                                    findNavController().navigate(action)
                                }
                            } else if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                findNavController().popBackStack(R.id.customizeMapDialog, false)
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.something_went_wrong),
                                Toast.LENGTH_SHORT,
                            ).show()
                            if (viewModel.isAisActivated.value) viewModel.toggleAisActivation()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.harboursFetchingState.collect { state ->
                    state ?: return@collect
                    if (!binding.chipHarbours.isChecked) return@collect

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
                                if (viewModel.isAisActivated.value) viewModel.toggleHarboursVisible()
                            }
                        }

                        null -> {
                            if (state.loading.isLoading) {
                                // show loading dialog
                                if (findNavController().currentDestination?.id == R.id.customizeMapDialog) {
                                    val action =
                                        CustomizeMapDialogDirections.actionCustomizeMapDialogToLoadingDialogFragment(
                                            getString(com.bytecause.core.resources.R.string.loading_harbours_text),
                                        )
                                    findNavController().navigate(action)
                                }

                                // update progress in loading dialog fragment
                                state.loading.progress?.let { progress ->
                                    loadingDialogSharedViewModel.updateProgress(progress)
                                }
                            } else if (findNavController().currentDestination?.id == R.id.loadingDialogFragment) {
                                findNavController().popBackStack(R.id.customizeMapDialog, false)
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.something_went_wrong),
                                Toast.LENGTH_SHORT,
                            ).show()
                            if (viewModel.isAisActivated.value) viewModel.toggleHarboursVisible()
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