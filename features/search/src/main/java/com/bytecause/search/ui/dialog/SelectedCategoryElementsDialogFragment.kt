package com.bytecause.search.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.PoiQueryModel
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.features.search.R
import com.bytecause.features.search.databinding.SelectedCategoryElementsFragmentLayoutBinding
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.model.PlaceType
import com.bytecause.presentation.model.UiState
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.search.ui.model.PoiUiModel
import com.bytecause.search.ui.viewmodel.SearchElementsSharedViewModel
import com.bytecause.search.ui.viewmodel.SelectedCategoryElementsViewModel
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.mappers.asLatLngModel
import com.bytecause.util.poi.PoiUtil.getCategoriesUnderUnifiedCategory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.io.IOException
import java.net.ConnectException


@AndroidEntryPoint
class SelectedCategoryElementsDialogFragment :
    DialogFragment(R.layout.selected_category_elements_fragment_layout) {
    private val binding by viewBinding(
        SelectedCategoryElementsFragmentLayoutBinding::inflate
    )

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: SelectedCategoryElementsViewModel by viewModels()
    private val sharedViewModel: SearchElementsSharedViewModel by activityViewModels()

    private val args: SelectedCategoryElementsDialogFragmentArgs by navArgs()

    private lateinit var recyclerView: RecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<PoiUiModel>

    private var filterJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val bindingInterface =
            object : com.bytecause.util.bindings.RecyclerViewBindingInterface<PoiUiModel> {
                override fun bindData(
                    item: PoiUiModel,
                    itemView: View,
                    itemPosition: Int,
                ) {
                    val innerItemView: LinearLayout =
                        itemView.findViewById(com.bytecause.core.presentation.R.id.recycler_view_inner_item_view)
                    val placeImage: ImageView =
                        itemView.findViewById(com.bytecause.core.presentation.R.id.place_image_view)
                    val placeName: TextView =
                        itemView.findViewById(com.bytecause.core.presentation.R.id.place_name_text_view)
                    val distance: TextView =
                        itemView.findViewById(com.bytecause.core.presentation.R.id.distance_textview)

                    innerItemView.setOnClickListener {
                        mapSharedViewModel.setSearchPlace(
                            PlaceType.Poi(
                                id = item.id,
                                latitude = item.lat,
                                longitude = item.lon,
                                name = innerItemView.findViewById<TextView>(com.bytecause.core.presentation.R.id.place_name_text_view).text.toString()
                            )
                        )
                        // Set filter StateFlow to null to reset it's state.
                        sharedViewModel.setFilter(null)
                        sharedViewModel.resetTags()

                        findNavController().popBackStack(
                            R.id.search_navigation,
                            true
                        )
                    }

                    placeImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            args.poiCategory.drawableId,
                        ),
                    )
                    placeName.text =
                        viewModel.getItemName(item, getString(args.poiCategory.name))
                            ?.replaceFirstChar { it.uppercase() }
                    distance.text =
                        if (mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() != null) {
                            com.bytecause.util.string.StringUtil.formatDistanceDoubleToString(
                                LatLng(
                                    item.lat,
                                    item.lon,
                                ).distanceTo(
                                    mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                        ?: return,
                                ),
                            )
                        } else {
                            ""
                        }
                }
            }

        genericRecyclerViewAdapter =
            GenericRecyclerViewAdapter(
                emptyList(),
                com.bytecause.core.presentation.R.layout.searched_places_recycler_view_item_view,
                bindingInterface
            )

        isCancelable = false

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // If list is not empty, add all values to elementList, otherwise make API call.
        viewModel.categoryElementsSet.takeIf { it.isNotEmpty() }?.let { categorySet ->
            viewModel.addElements(categorySet)
        } ?: run {
            mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let {
                populateRecyclerView()
            } ?: run {
                toggleLocationUnknownLayout()
            }
        }

        binding.extendSearchRadiusLayout.extendSearchRadiusClickableTextView.setOnClickListener {
            if (viewModel.radius >= 960000) return@setOnClickListener

            viewModel.doubleSearchRadius()
            // Update text views
            updateExtendSearchRadiusLayout()
            populateRecyclerView()
        }

        binding.showResultsOnTheMapRelativeLayout.setOnClickListener {
            mapSharedViewModel.setPoiToShow(
                viewModel.elementSet.value.let { elementSet ->
                    mapOf(getString(args.poiCategory.name) to elementSet.map { it.id })
                }
            )

            sharedViewModel.setFilter(null)
            sharedViewModel.resetTags()
            findNavController().popBackStack(
                R.id.search_navigation,
                true
            )
        }

        recyclerView =
            binding.categoryElementsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericRecyclerViewAdapter
            }

        binding.categoryNameTextView.text = getString(args.poiCategory.name)

        binding.backButton.setOnClickListener {
            // Set filter StateFlow to null to reset it's state.
            sharedViewModel.setFilter(null)
            sharedViewModel.resetTags()
            // Reset allTagsSharedFlow state.
            sharedViewModel.setTags(emptyMap())

            findNavController().popBackStack()
        }

        binding.filterListImageButton.setOnClickListener {
            if (!com.bytecause.util.common.LastClick()
                    .lastClick(1000) || filterJob?.isCompleted == false || binding.progressLayout.isVisible
            ) return@setOnClickListener

            filterJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    sharedViewModel.allTagsSharedFlow.replayCache.lastOrNull().let {
                        if (it.isNullOrEmpty()) {
                            // Save init map key value pairs.
                            viewModel.extractAllTags()
                            sharedViewModel.setTags(viewModel.allTagsMap)
                        }
                    }
                    findNavController().navigate(
                        R.id.action_selectedCategoryElementsDialogFragment_to_selectedCategoryElementsListFilterDialog,
                    )
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.elementSet.collect { elements ->

                    genericRecyclerViewAdapter.updateContent(elements.toList())
                    updateCount(elements.size)

                    if (viewModel.uiSearchCategoryState.value != UiState<PoiUiModel>(
                            loading = Loading(
                                isLoading = true
                            )
                        )
                    ) {
                        toggleExtendSearchRadiusLayout()
                    }
                    toggleShowResultsOnTheMapLayout()
                }
            }
        }

        // StateFlow consumer of Overpass API output
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiSearchCategoryState.collect { state ->
                    state ?: return@collect

                    when {
                        viewModel.elementSet.value.containsAll(state.items) && state.items.isNotEmpty() -> {
                            return@collect
                        }

                        state.items.isNotEmpty() &&
                                viewModel.elementSet.value
                                    .isNotEmpty() &&
                                !viewModel.elementSet.value
                                    .containsAll(
                                        state.items,
                                    ) -> {
                            viewModel.clearElements()
                        }
                    }

                    if (state.error != null) {
                        binding.progressLayout.visibility = View.GONE
                    }

                    when (val exception = state.error) {
                        is IOException -> {
                            binding.errorLayout.apply {
                                errorImageView.apply {
                                    setImageResource(
                                        if (exception is ConnectException) com.bytecause.core.resources.R.drawable.service_unavailable
                                        else com.bytecause.core.resources.R.drawable.network_error
                                    )
                                }
                                errorTextView.text =
                                    resources.getString(
                                        if (exception is ConnectException) com.bytecause.core.resources.R.string.service_unavailable
                                        else com.bytecause.core.resources.R.string.network_error
                                    )
                            }
                            showErrorLayout()
                        }

                        // Handle empty API response
                        is NoSuchElementException -> {
                            toggleExtendSearchRadiusLayout()
                        }

                        null -> {
                            if (state.loading.isLoading) {
                                binding.progressLayout.visibility = View.VISIBLE
                            } else {
                                binding.progressLayout.visibility = View.GONE
                            }

                            state.items.takeIf { it.isNotEmpty() }?.let { foundElementList ->
                                mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                    ?.let { position ->
                                        foundElementList
                                            .toSortedSet(compareBy { poi ->
                                                LatLng(
                                                    poi.lat,
                                                    poi.lon,
                                                ).distanceTo(position)
                                            })
                                            .let { sortedSet ->
                                                viewModel.addAllToCategoryElementsSet(sortedSet)
                                                sharedViewModel.filteredTagsStateFlow.value.takeIf { !it.isNullOrEmpty() }
                                                    ?.let { appliedFilters ->
                                                        viewModel.addElements(
                                                            viewModel.filterAlgorithm(
                                                                appliedFilters
                                                            )
                                                        )
                                                    } ?: run {
                                                    viewModel.addElements(sortedSet)
                                                }
                                            }
                                    }
                            } ?: run {
                                if (state.loading.isLoading) return@run

                                binding.categoryElementsRecyclerView.visibility = View.GONE
                                toggleExtendSearchRadiusLayout()
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(com.bytecause.core.resources.R.string.something_went_wrong),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }

        // StateFlow consumer for currently applied filters from FilterDialog
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.filteredTagsStateFlow.collect { filterTags ->
                    filterTags ?: return@collect

                    // This list will supply recyclerView with filtered elements.
                    val filteredList = mutableListOf<PoiUiModel>()

                    if (filterTags.isEmpty()) {
                        // If no filter applied update drawable.
                        binding.filterListImageButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.filter_off_icon,
                            ),
                        )
                        filteredList.addAll(viewModel.categoryElementsSet)
                    } else {
                        // If filter is applied update drawable.
                        binding.filterListImageButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.filter_on_icon,
                            ),
                        )
                    }

                    // Update the UI with the filtered list.
                    viewModel.clearElements()
                    viewModel.addElements(viewModel.filterAlgorithm(filterTags))

                    viewModel.updateCheckedStatus(viewModel.getTagsOfVisibleItems(), filterTags)
                        .let {
                            sharedViewModel.setTags(viewModel.sortMap(it))
                        }
                    toggleExtendSearchRadiusLayout()
                }
            }
        }
    }

    private fun toggleShowResultsOnTheMapLayout() {
        if (genericRecyclerViewAdapter.itemCount == 0) {
            if (binding.showResultsOnTheMapRelativeLayout.visibility == View.GONE) return
            binding.showResultsOnTheMapRelativeLayout.visibility = View.GONE
        } else {
            if (binding.showResultsOnTheMapRelativeLayout.visibility == View.VISIBLE) return
            binding.showResultsOnTheMapRelativeLayout.visibility = View.VISIBLE
            binding.showResultsOnTheMapTextView.text =
                getString(com.bytecause.core.resources.R.string.show_results_on_the_map_place_holder).format(
                    getString(args.poiCategory.name),
                )
        }
    }

    private fun populateRecyclerView() {
        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let { latLng ->
            viewModel.getPoiResult(
                PoiQueryModel(
                    categoryList = getCategoriesUnderUnifiedCategory(args.poiCategory.name)
                        ?: listOf(
                            getString(args.poiCategory.name)
                        ),
                    radius = viewModel.radius,
                    position = latLng.asLatLngModel(),
                    query = OverpassQueryBuilder
                        .format(OverpassQueryBuilder.FormatTypes.JSON)
                        .timeout(120)
                        .radius()
                        .search(
                            OverpassQueryBuilder.Type.Node,
                            args.poiCategory.search
                        )
                        .area(viewModel.radius, latLng.asLatLngModel())
                        .build(),
                    appliedFilters = sharedViewModel.filteredTagsStateFlow.value,
                )
            )
        }
    }

    private fun updateCount(count: Int) {
        binding.countTextView.text =
            getString(com.bytecause.core.resources.R.string.count_text_place_holder).format(
                count,
            )
    }

    private fun toggleLocationUnknownLayout() {
        binding.categoryElementsRecyclerView.visibility = View.GONE
        binding.locationUnknownLayout.locationUnknownLinearLayout.visibility = View.VISIBLE
    }

    private fun toggleExtendSearchRadiusLayout() {
        if (binding.progressLayout.visibility == View.VISIBLE
            || binding.locationUnknownLayout.locationUnknownLinearLayout.visibility == View.VISIBLE
        ) return

        if (viewModel.elementSet.value.size < 15) {
            binding.extendSearchRadiusLayout.extendSearchRadiusLinearLayout.visibility =
                View.VISIBLE
            updateExtendSearchRadiusLayout()
        } else if (binding.extendSearchRadiusLayout.extendSearchRadiusLinearLayout.visibility == View.VISIBLE) {
            binding.extendSearchRadiusLayout.extendSearchRadiusLinearLayout.visibility = View.GONE
            viewModel.resetSearchRadius()
        }
    }

    private fun updateExtendSearchRadiusLayout() {
        binding.extendSearchRadiusLayout.extendSearchRadiusLayoutTitle.text =
            getString(com.bytecause.core.resources.R.string.nothing_found_text_place_holder).format(
                if (viewModel.elementSet.value.isEmpty()) getString(com.bytecause.core.resources.R.string.nothing) else viewModel.elementSet.value.size,
                viewModel.radius / 1000,
                "km",
            )
        binding.extendSearchRadiusLayout.extendSearchRadiusClickableTextView.text =
            getString(com.bytecause.core.resources.R.string.extend_search_radius_text_place_holder).format(
                if (viewModel.radius < 960000) (viewModel.radius / 1000) * 2 else viewModel.radius / 1000,
                "km",
            )
    }

    private fun showErrorLayout() {
        if (binding.errorLayout.networkErrorLinearLayout.visibility == View.VISIBLE) return
        binding.categoryElementsRecyclerView.visibility = View.GONE
        binding.errorLayout.networkErrorLinearLayout.visibility = View.VISIBLE
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
                    com.bytecause.core.resources.R.color.md_theme_primaryContainer,
                ),
            ),
        )
    }

    /*private fun hideErrorLayout() {
        if (binding.errorLayout.networkErrorLinearLayout.visibility == View.GONE) return
        binding.errorLayout.networkErrorLinearLayout.visibility = View.GONE
        binding.categoryElementsRecyclerView.visibility = View.VISIBLE
    }*/
}
