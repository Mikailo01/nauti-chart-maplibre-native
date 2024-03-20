package com.bytecause.nautichart.ui.view.fragment.dialog

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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.nautichart.databinding.SelectedCategoryElementsFragmentLayoutBinding
import com.bytecause.nautichart.domain.model.OverpassNodeModel
import com.bytecause.nautichart.domain.model.PoiQueryEntity
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.ui.adapter.GenericRecyclerViewAdapter
import com.bytecause.nautichart.ui.adapter.RecyclerViewBindingInterface
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.ui.viewmodels.SearchElementsSharedViewModel
import com.bytecause.nautichart.ui.viewmodels.SelectedCategoryElementsViewModel
import com.bytecause.nautichart.util.PoiUtil
import com.bytecause.nautichart.util.SimpleOverpassQueryBuilder
import com.bytecause.nautichart.util.StringUtil
import com.bytecause.nautichart.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint


@AndroidEntryPoint
class SelectedCategoryElementsDialogFragment :
    DialogFragment(R.layout.selected_category_elements_fragment_layout) {

    private val binding by viewBinding(SelectedCategoryElementsFragmentLayoutBinding::inflate)

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: SelectedCategoryElementsViewModel by viewModels()
    private val sharedViewModel: SearchElementsSharedViewModel by activityViewModels()

    private val args: SelectedCategoryElementsDialogFragmentArgs by navArgs()

    private lateinit var recyclerView: RecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<OverpassNodeModel>

    //private val elementList = mutableListOf<OverpassNodeModel>()

    private var filterJob: Job? = null

    private var searchBiggerRadiusJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val bindingInterface = object : RecyclerViewBindingInterface<OverpassNodeModel> {
            override fun bindData(item: OverpassNodeModel, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(R.id.recycler_view_inner_item_view)
                val placeImage: ImageView = itemView.findViewById(R.id.place_image_view)
                val placeName: TextView = itemView.findViewById(R.id.place_name_text_view)
                val distance: TextView = itemView.findViewById(R.id.distance_textview)

                innerItemView.setOnClickListener {
                    mapSharedViewModel.setPlaceToFind(
                        SearchPlaceCacheEntity(
                            placeId = item.id.toString(),
                            latitude = item.lat,
                            longitude = item.lon
                        )
                    )
                    // Set filter StateFlow to null to reset it's state.
                    sharedViewModel.setFilter(null)
                    sharedViewModel.resetTags()
                    findNavController().popBackStack(R.id.map_dest, false)
                }

                placeImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        args.poiCategory.drawableId
                    )
                )
                placeName.text = viewModel.getItemName(item, args.poiCategory.name)
                    ?.replaceFirstChar { it.uppercase() }
                distance.text =
                    if (mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() != null) StringUtil.formatDistanceDoubleToString(
                        GeoPoint(
                            item.lat,
                            item.lon
                        ).distanceToAsDouble(mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull())
                    ) else ""
            }
        }

        genericRecyclerViewAdapter = GenericRecyclerViewAdapter(
            viewModel.elementList.value.toList(),
            R.layout.searched_places_recycler_view_item_view,
            bindingInterface
        )

        this.isCancelable = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If list is not empty, add all values to elementList, otherwise make API call.
        viewModel.categoryElementsList.takeIf { it.isNotEmpty() }?.let { categoryList ->
            viewModel.addElements(categoryList)
        } ?: run {
            mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let {
                populateRecyclerView()
            } ?: run {
                toggleLocationUnknownLayout()
            }
        }

        binding.extendSearchRadiusLayout.extendSearchRadiusClickableTextView.setOnClickListener {
            if (viewModel.radius >= 960000) return@setOnClickListener
            if (searchBiggerRadiusJob?.isCompleted == false) return@setOnClickListener

            viewModel.modifySearchRadius(viewModel.radius * 2)
            // Update text views
            updateExtendSearchRadiusLayout()
            populateRecyclerView()
        }

        binding.showResultsOnTheMapRelativeLayout.setOnClickListener {
            mapSharedViewModel.setPoiToShow(
                viewModel.elementList.value
                    .groupBy { args.poiCategory.name }
                    .mapValues { entry -> entry.value.map { it.id } }
            )

            sharedViewModel.setFilter(null)
            sharedViewModel.resetTags()
            findNavController().popBackStack(R.id.map_dest, false)
        }

        recyclerView = binding.categoryElementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = genericRecyclerViewAdapter
        }

        binding.categoryNameTextView.text = args.poiCategory.name

        binding.backButton.setOnClickListener {
            // Set filter StateFlow to null to reset it's state.
            sharedViewModel.setFilter(null)
            sharedViewModel.resetTags()
            // Reset allTagsSharedFlow state.
            sharedViewModel.setTags(emptyMap())

            findNavController().popBackStack()
        }

        binding.filterListImageButton.setOnClickListener {
            if (!Util.lastClick(1000) || filterJob?.isCompleted == false || binding.progressLayout.isVisible) return@setOnClickListener

            filterJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                sharedViewModel.allTagsSharedFlow.replayCache.lastOrNull().let {
                    if (it.isNullOrEmpty()) {
                        // Save init map key value pairs.
                        viewModel.extractAllTags()
                        sharedViewModel.setTags(viewModel.allTagsMap)
                    }
                }
                findNavController().navigate(R.id.action_selectedCategoryElementsDialogFragment_to_selectedCategoryElementsListFilterDialog)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.elementList.collect { elements ->
                    genericRecyclerViewAdapter.updateContent(elements.toList())

                    updateCount(elements.size)
                    toggleShowResultsOnTheMapLayout()
                }
            }
        }

        // StateFlow consumer of Overpass API output
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiSearchCategoryState.collect { state ->
                    state ?: return@collect

                    if (viewModel.elementList.value
                            .containsAll(state.items) && state.items.isNotEmpty()
                    ) {
                        return@collect
                    } else if (state.items.isNotEmpty() && viewModel.elementList.value
                            .isNotEmpty() && !viewModel.elementList.value
                            .containsAll(
                                state.items
                            )
                    ) {
                        viewModel.clearElements()
                    }

                    if (state.error != null) {
                        binding.progressLayout.visibility = View.GONE
                    }

                    when (state.error) {
                        is UiState.Error.NetworkError -> {
                            binding.errorLayout.apply {
                                errorImageView.apply {
                                    setImageResource(R.drawable.network_error)
                                }
                                errorTextView.text = resources.getString(R.string.network_error)
                            }
                            showErrorLayout()
                        }

                        is UiState.Error.ServiceUnavailable -> {
                            binding.errorLayout.apply {
                                errorImageView.apply {
                                    alpha = 0.5F
                                    setImageResource(R.drawable.service_unavailable)
                                }
                                errorTextView.text =
                                    resources.getString(R.string.service_unavailable)
                            }
                            showErrorLayout()
                        }

                        is UiState.Error.Other -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        null -> {
                            if (state.isLoading) binding.progressLayout.visibility = View.VISIBLE
                            else binding.progressLayout.visibility = View.GONE

                            state.items.takeIf { it.isNotEmpty() }?.let { foundElementList ->
                                mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                                    ?.let { position ->
                                        foundElementList.sortedBy { elementToSort ->
                                            GeoPoint(
                                                elementToSort.lat,
                                                elementToSort.lon
                                            ).distanceToAsDouble(position)
                                        }.let { sortedList ->
                                            viewModel.addAllToCategoryElementsList(sortedList)
                                            sharedViewModel.filteredTagsStateFlow.value.takeIf { !it.isNullOrEmpty() }
                                                ?.let { appliedFilters ->
                                                    viewModel.addElements(
                                                        viewModel.filterAlgorithm(
                                                            appliedFilters
                                                        )
                                                    )
                                                } ?: run {
                                                viewModel.addElements(sortedList)
                                                toggleExtendSearchRadiusLayout()
                                            }
                                        }
                                    }
                            } ?: run {
                                if (state.isLoading) return@run
                                binding.categoryElementsRecyclerView.visibility = View.GONE
                                toggleExtendSearchRadiusLayout()
                            }
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
                    val filteredList = mutableListOf<OverpassNodeModel>()

                    if (filterTags.isEmpty()) {
                        // If no filter applied update drawable.
                        binding.filterListImageButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.filter_off_icon
                            )
                        )
                        filteredList.addAll(viewModel.categoryElementsList)
                    } else {
                        // If filter is applied update drawable.
                        binding.filterListImageButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.filter_on_icon
                            )
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
                getString(R.string.show_results_on_the_map_place_holder).format(
                    args.poiCategory.name
                )
        }
    }

    private fun populateRecyclerView() {
        mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()?.let { geoPoint ->
            viewModel.getPoiResult(
                PoiQueryEntity(
                    category = PoiUtil.unifyPoiCategory(args.poiCategory.name),
                    radius = viewModel.radius,
                    position = geoPoint,
                    query = SimpleOverpassQueryBuilder(
                        format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                        timeoutInSeconds = 120,
                        type = "node",
                        radiusInMeters = viewModel.radius,
                        geoPoint = geoPoint,
                        search = args.poiCategory.search
                    ),
                    appliedFilters = sharedViewModel.filteredTagsStateFlow.value
                )
            )
        }
    }

    private fun updateCount(count: Int) {
        binding.countTextView.text = getString(R.string.count_text_place_holder).format(
            count
        )
    }

    private fun toggleLocationUnknownLayout() {
        binding.categoryElementsRecyclerView.visibility = View.GONE
        binding.locationUnknownLayout.locationUnknownLinearLayout.visibility = View.VISIBLE
    }

    private fun toggleExtendSearchRadiusLayout() {
        if (binding.progressLayout.visibility == View.VISIBLE) return
        if (viewModel.elementList.value.size < 15) {
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
            getString(R.string.nothing_found_text_place_holder).format(
                if (viewModel.elementList.value.isEmpty()) "Nothing" else viewModel.elementList.value.size,
                viewModel.radius / 1000,
                "km"
            )
        binding.extendSearchRadiusLayout.extendSearchRadiusClickableTextView.text =
            getString(R.string.extend_search_radius_text_place_holder).format(
                if (viewModel.radius < 960000) (viewModel.radius / 1000) * 2 else viewModel.radius / 1000,
                "km"
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

    /*private fun hideErrorLayout() {
        if (binding.errorLayout.networkErrorLinearLayout.visibility == View.GONE) return
        binding.errorLayout.networkErrorLinearLayout.visibility = View.GONE
        binding.categoryElementsRecyclerView.visibility = View.VISIBLE
    }*/


}