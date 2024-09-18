package com.bytecause.search.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecause.domain.model.ArgsObjectTypeArray
import com.bytecause.features.search.R
import com.bytecause.features.search.databinding.SearchHistoryFragmentBinding
import com.bytecause.presentation.components.views.dialog.ConfirmationDialog
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.model.PlaceType
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.search.mapper.asRecentlySearchedPlaceUiModel
import com.bytecause.search.ui.viewmodel.SearchHistoryViewModel
import com.bytecause.util.KeyboardUtils
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.poi.PoiUtil.assignDrawableToAddressType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng


@AndroidEntryPoint
class SearchHistoryFragment : Fragment(R.layout.search_history_fragment),
    ConfirmationDialog.ConfirmationDialogListener {

    private val binding by viewBinding(SearchHistoryFragmentBinding::bind)

    private val viewModel: SearchHistoryViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<SearchedPlaceUiModel>

    private var isKeyboardVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bindingInterface = object :
            com.bytecause.util.bindings.RecyclerViewBindingInterface<SearchedPlaceUiModel> {
            override fun bindData(item: SearchedPlaceUiModel, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.recycler_view_inner_item_view)
                val placeImage: ImageView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.place_image_view)
                val placeName: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.place_name_text_view)
                val distanceText: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.distance_textview)

                innerItemView.setOnClickListener {
                    item.let { searchedPlace ->

                        viewModel.saveRecentlySearchedPlace(
                            searchedPlace.asRecentlySearchedPlaceUiModel()
                        )

                        viewLifecycleOwner.lifecycleScope.launch {
                            // Make query to get needed element using AppSearch API.
                            viewModel.searchCachedResult(
                                searchedPlace.name.takeIf { it.isNotEmpty() }
                                    ?: searchedPlace.displayName
                            ).firstOrNull()
                                ?.first { it.placeId == searchedPlace.placeId }
                                ?.let {
                                    mapSharedViewModel.setSearchPlace(PlaceType.Address(it))

                                    findNavController().popBackStack(
                                        R.id.search_navigation,
                                        true
                                    )
                                }

                        }
                    }
                }
                innerItemView.setOnLongClickListener {
                    val dialog = ConfirmationDialog.newInstance(
                        resources.getString(com.bytecause.core.resources.R.string.delete_recent_searched_element),
                        null,
                        ArgsObjectTypeArray.IntType(itemPosition),
                        "delete_recent"
                    )
                    dialog.show(childFragmentManager, "ConfirmationDialog")
                    return@setOnLongClickListener true
                }
                placeImage.setImageResource(
                    assignDrawableToAddressType(item.addressType)
                )
                placeName.text =
                    if (item.displayName.startsWith(item.name)) item.displayName else item.name
                distanceText.text =
                    if (mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() != null) com.bytecause.util.string.StringUtil.formatDistanceDoubleToString(
                        LatLng(
                            item.latitude,
                            item.longitude
                        ).distanceTo(
                            mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() ?: return
                        )
                    ) else ""
            }
        }

        genericRecyclerViewAdapter = GenericRecyclerViewAdapter(
            emptyList(),
            com.bytecause.core.presentation.R.layout.searched_places_recycler_view_item_view,
            bindingInterface
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecentlySearchedPlaceList.collect { searchedPlaces ->

                    searchedPlaces
                        .sortedByDescending { element -> element.timestamp }
                        .map { searchedPlace ->
                            SearchedPlaceUiModel(
                                placeId = searchedPlace.placeId,
                                latitude = searchedPlace.latitude,
                                longitude = searchedPlace.longitude,
                                addressType = searchedPlace.type,
                                name = searchedPlace.name,
                                displayName = searchedPlace.displayName
                            )
                        }
                        .take(8)
                        .toList()
                        .let { list ->
                            // if sequence has more than 7 elements, show TextView and drop last element
                            // because only 7 elements should be rendered in the recycler view
                            if (list.size > 7) {
                                binding.showAllPlacesTextView.visibility = View.VISIBLE
                                genericRecyclerViewAdapter.updateContent(list.dropLast(1))
                            } else {
                                binding.showAllPlacesTextView.visibility = View.GONE
                                genericRecyclerViewAdapter.updateContent(list)
                            }
                        }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        KeyboardUtils.addKeyboardToggleListener(this@SearchHistoryFragment.activity) { isVisible ->
            isKeyboardVisible = isVisible
        }

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!isKeyboardVisible) return@setOnTouchListener false
                KeyboardUtils.forceCloseKeyboard(v)
                v.performClick()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        val linearLayoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        recyclerView = binding.searchHistoryRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = genericRecyclerViewAdapter
        }

        binding.showAllPlacesTextView.setOnClickListener {
            findNavController().navigate(R.id.action_searchMapFragmentDialog_to_fullSearchHistoryListDialog)
        }
    }

    override fun onDialogPositiveClick(dialogId: String, additionalData: Any?) {
        (additionalData as? ArgsObjectTypeArray.IntType)?.value.let { position ->
            position ?: return

            viewModel.deleteRecentlySearchedPlace((viewModel.dataStoreSize - 1) - position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KeyboardUtils.removeAllKeyboardToggleListeners()
    }
}