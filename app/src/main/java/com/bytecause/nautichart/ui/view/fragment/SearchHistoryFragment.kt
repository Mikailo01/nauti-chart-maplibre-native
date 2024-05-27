package com.bytecause.nautichart.ui.view.fragment

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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.databinding.SearchHistoryFragmentBinding
import com.bytecause.nautichart.domain.model.ArgsObjectTypeArray
import com.bytecause.nautichart.domain.model.SearchedPlace
import com.bytecause.nautichart.ui.adapter.GenericRecyclerViewAdapter
import com.bytecause.nautichart.ui.adapter.RecyclerViewBindingInterface
import com.bytecause.nautichart.ui.util.DrawableUtil
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.view.fragment.dialog.ConfirmationDialog
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.ui.viewmodels.SearchHistoryViewModel
import com.bytecause.nautichart.util.KeyboardUtils
import com.bytecause.nautichart.util.StringUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.properties.Delegates


/**
 * http://neethack.com/2017/04/Why-inheritance-is-bad/
 * **/

// TODO("Refactor")
@AndroidEntryPoint
class SearchHistoryFragment : Fragment(R.layout.search_history_fragment),
    ConfirmationDialog.ConfirmationDialogListener {

    private val binding by viewBinding(SearchHistoryFragmentBinding::bind)

    private val viewModel: SearchHistoryViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<SearchedPlace>

    private val historyList = mutableListOf<SearchedPlace>()

    /* Because historyList is limited to 7 elements and datastore is not,
       then we have to know the total count of elements in datastore during removing process. */
    private var dataStoreSize by Delegates.notNull<Int>()

    private var isKeyboardVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bindingInterface = object : RecyclerViewBindingInterface<SearchedPlace> {
            override fun bindData(item: SearchedPlace, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(R.id.recycler_view_inner_item_view)
                val placeImage: ImageView = itemView.findViewById(R.id.place_image_view)
                val placeName: TextView = itemView.findViewById(R.id.place_name_text_view)
                val distanceText: TextView = itemView.findViewById(R.id.distance_textview)

                innerItemView.setOnClickListener {
                    item.let { searchedPlace ->
                        updateRecentlySearchedPlaces(
                            RecentlySearchedPlace.newBuilder()
                                .setPlaceId(searchedPlace.placeId)
                                .setLatitude(searchedPlace.latitude)
                                .setLongitude(searchedPlace.longitude)
                                .setName(searchedPlace.name)
                                .setDisplayName(searchedPlace.displayName)
                                .setType(searchedPlace.addressType)
                                .setTimeStamp(System.currentTimeMillis())
                                .build()
                        )
                        mapSharedViewModel.apply {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                // Make query to get needed element using AppSearch API.
                                viewModel.searchCachedResult(searchedPlace.name.takeIf { it.isNotEmpty() }
                                    ?: searchedPlace.displayName).first {
                                    it.placeId.toLong() == searchedPlace.placeId
                                }.let {
                                    mapSharedViewModel.setPlaceToFind(it)
                                    setDismissSearchMapDialogState(true)
                                }

                            }
                        }
                    }
                }
                innerItemView.setOnLongClickListener {
                    val dialog = ConfirmationDialog.newInstance(
                        resources.getString(R.string.delete_recent_searched_element),
                        null,
                        ArgsObjectTypeArray.IntType(itemPosition),
                        "delete_recent"
                    )
                    dialog.show(childFragmentManager, "ConfirmationDialog")
                    return@setOnLongClickListener true
                }
                placeImage.setImageResource(
                    DrawableUtil.assignDrawableToAddressType(item.addressType)
                )
                placeName.text =
                    if (item.displayName.startsWith(item.name)) item.displayName else item.name
                distanceText.text =
                    if (mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() != null) StringUtil.formatDistanceDoubleToString(
                        GeoPoint(
                            item.latitude,
                            item.longitude
                        ).distanceToAsDouble(mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull())
                    ) else ""
            }
        }

        genericRecyclerViewAdapter = GenericRecyclerViewAdapter(
            historyList,
            R.layout.searched_places_recycler_view_item_view,
            bindingInterface
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecentlySearchedPlaceList.collect {
                    it ?: return@collect

                    historyList.clear()

                    if (!it.placeList.isNullOrEmpty()) {
                        dataStoreSize = it.placeCount
                        it.placeList.sortedByDescending { element -> element.timeStamp }
                            .forEach { place ->
                                val element = SearchedPlace(
                                    placeId = place.placeId,
                                    latitude = place.latitude,
                                    longitude = place.longitude,
                                    addressType = place.type,
                                    name = place.name,
                                    displayName = place.displayName
                                )
                                if (historyList.size < 7) {
                                    if (binding.showAllPlacesTextView.visibility == View.VISIBLE) binding.showAllPlacesTextView.visibility =
                                        View.GONE
                                    historyList.add(element)
                                } else {
                                    binding.showAllPlacesTextView.visibility = View.VISIBLE
                                }
                            }
                        genericRecyclerViewAdapter.notifyItemRangeChanged(0, historyList.size)
                    } else return@collect
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

        //view.setOnTouchListener(HideKeyboardOnTouchListener(requireContext()))

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

    private fun updateRecentlySearchedPlaces(element: RecentlySearchedPlace) {
        element.let {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getRecentlySearchedPlaceList.firstOrNull()
                    .let { savedPlaces ->
                        savedPlaces ?: return@launch

                        val updatedList =
                            (savedPlaces.placeList.filter { place -> place.placeId != it.placeId } + it)
                        viewModel.updateRecentlySearchedPlaces(updatedList)
                    }
            }
        }
    }

    override fun onDialogPositiveClick(dialogId: String, additionalData: Any?) {
        (additionalData as? Int).let { position ->
            position ?: return
            viewModel.deleteRecentlySearchedPlace((dataStoreSize - 1) - position)
            historyList.removeAt((genericRecyclerViewAdapter.itemCount - 1) - position)
            genericRecyclerViewAdapter.notifyItemRemoved(position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KeyboardUtils.removeAllKeyboardToggleListeners()
    }
}