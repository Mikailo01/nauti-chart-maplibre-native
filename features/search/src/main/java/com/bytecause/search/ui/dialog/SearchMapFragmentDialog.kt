package com.bytecause.search.ui.dialog

import android.content.DialogInterface
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bytecause.core.resources.R
import com.bytecause.features.search.databinding.SearchMapFragmentDialogBinding
import com.bytecause.presentation.components.views.CustomTextInputEditText
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.model.PlaceType
import com.bytecause.presentation.model.SearchedPlaceUiModel
import com.bytecause.presentation.model.UiState
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.search.ui.SearchHistoryFragment
import com.bytecause.search.ui.SearchMapCategoriesFragment
import com.bytecause.search.ui.adapter.SearchMapViewPagerAdapter
import com.bytecause.search.ui.viewmodel.SearchMapViewModel
import com.bytecause.util.KeyboardUtils
import com.bytecause.util.context.getProgressBarDrawable
import com.bytecause.util.context.hideKeyboard
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.poi.PoiUtil.assignDrawableToAddressType
import com.bytecause.util.string.StringUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import java.io.IOException
import java.net.ConnectException


@AndroidEntryPoint
class SearchMapFragmentDialog : DialogFragment() {

    private val binding by viewBinding(SearchMapFragmentDialogBinding::inflate)

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: SearchMapViewModel by viewModels()

    private lateinit var recyclerView: FullyExpandedRecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<SearchedPlaceUiModel>

    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPagerAdapter: SearchMapViewPagerAdapter
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.bytecause.features.search.R.style.Theme_NautiChart)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val linearLayoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        val bindingInterface = object :
            com.bytecause.util.bindings.RecyclerViewBindingInterface<SearchedPlaceUiModel> {
            override fun bindData(item: SearchedPlaceUiModel, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.recycler_view_inner_item_view)
                val placeImage: ImageView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.place_image_view)
                val placeName: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.place_name_text_view)
                val distance: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.distance_textview)

                innerItemView.setOnClickListener {
                    viewModel.saveAndShowSearchedPlace(item)
                }

                placeImage.setImageResource(
                    assignDrawableToAddressType(item.addressType)
                )
                placeName.text =
                    ContextCompat.getString(requireContext(), R.string.split_two_strings_formatter)
                        .format(
                            if (item.displayName.startsWith(item.name)) item.displayName else item.name,
                            item.addressType.replaceFirstChar { it.uppercase() }
                        )
                distance.text =
                    if (mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull() != null) StringUtil.formatDistanceDoubleToString(
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
            listOf(),
            com.bytecause.core.presentation.R.layout.searched_places_recycler_view_item_view,
            bindingInterface
        )

        recyclerView = binding.searchedPlacesRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = genericRecyclerViewAdapter
        }

        val fragmentList = listOf(SearchHistoryFragment(), SearchMapCategoriesFragment())

        binding.searchViewPager.viewPager

        viewPagerAdapter =
            SearchMapViewPagerAdapter(fragmentList, childFragmentManager, this.lifecycle)
        viewPager2 = binding.searchViewPager.viewPager.apply {
            adapter = viewPagerAdapter
        }
        tabLayout = binding.searchMapTabLayout.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    requireContext().hideKeyboard(tab?.view)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    requireContext().hideKeyboard(tab?.view)
                }
            })
        }

        this.isCancelable = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open AppSearch API session.
        viewModel.initSession()

        val clearTextDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_close_24)?.apply {
                setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_theme_onPrimaryContainer
                    )
                )
            }
        val progressDrawable = requireContext().getProgressBarDrawable().apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding.searchMapBox.searchMapEditText.apply {

            // Set navigate back arrow in custom edit text view.
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_back_24)?.apply {
                setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_theme_onPrimaryContainer
                    )
                )
                setDrawables(this, null)
            }

            setPaddingRelative(
                30,
                paddingTop,
                paddingEnd,
                paddingBottom
            )
            compoundDrawablePadding = 30

            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer).let {
                setHintTextColor(it)
                setTextColor(it)
            }

            requestFocus()
            postDelayed({
                KeyboardUtils.toggleKeyboardVisibility(requireContext())
            }, 400)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Handle the action (e.g., perform search)
                    KeyboardUtils.forceCloseKeyboard(view)
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            val handler = Handler(Looper.getMainLooper())

            setOnTextChangedListener(object : CustomTextInputEditText.OnTextChangedListener {
                override fun onTextChanged(text: CharSequence?) {
                    hideErrorLayout()
                    genericRecyclerViewAdapter.updateContent(listOf())
                    if (text.isNullOrEmpty() && genericRecyclerViewAdapter.itemCount == 0) {
                        // Clear right drawable when empty.
                        setDrawables(
                            right = null,
                        )
                        tabLayout.visibility = View.VISIBLE
                        viewPager2.visibility = View.VISIBLE
                        return
                    }
                    // Show right drawable if not empty.
                    setDrawables(
                        right = clearTextDrawable,
                    )
                    tabLayout.visibility = View.GONE
                    viewPager2.visibility = View.GONE
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (text.isNullOrEmpty()) return@postDelayed
                        // TODO("Search by coordinates fix")
                        /* if (MapUtil.areCoordinatesValid(text.toString())) {
                             searchByCoordinates(text.toString())
                             return@postDelayed
                         }*/
                        viewModel.searchPlaces(text.toString())
                    }, 1000)
                }
            })

            setOnDrawableClickListener(object :
                CustomTextInputEditText.OnDrawableClickListener {
                override fun onStartDrawableClick(view: CustomTextInputEditText) {
                    //   findNavController().popBackStack(R.id.map_dest, false)
                    findNavController().popBackStack()
                }

                override fun onEndDrawableClick(view: CustomTextInputEditText) {
                    binding.searchMapBox.searchMapEditText.let {
                        it.text ?: return
                        if (viewModel.isLoading) return
                        it.text = null
                    }
                }
            })
        }

        binding.searchMapBox.searchMapLayout.boxBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.md_theme_primaryContainer)

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.history)
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.baseline_history_24)
                }

                1 -> {
                    tab.text = resources.getString(R.string.categories)
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.baseline_category_24)
                }
            }
        }.attach()

        // hides search map dialog, after tapping on element in search history (history content is
        // inflated in different fragment, so I needed to notify parent fragment dialog which serves
        // as container.)
        viewLifecycleOwner.lifecycleScope.launch {
            mapSharedViewModel.dismissSearchMapDialog.collect {
                it ?: return@collect
                findNavController().popBackStack()
            }
        }

        // data returned by api or cache database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiSearchState.collect {
                    it ?: return@collect
                    if (it.loading.isLoading) {
                        binding.searchMapBox.searchMapEditText.setDrawables(right = progressDrawable)
                        (progressDrawable as? Animatable)?.start()
                    } else {
                        binding.searchMapBox.searchMapEditText.setDrawables(right = clearTextDrawable)
                    }
                    populateRecyclerView(it)
                }
            }
        }

        // we have to notify MapFragment that the selected place should be drawn on map.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.searchPlace.collect {
                    mapSharedViewModel.setPlaceToFind(PlaceType.Address(it))
                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun showErrorLayout() {
        if (binding.errorLayout.networkErrorLinearLayout.visibility == View.VISIBLE) return
        binding.searchedPlacesRecyclerView.visibility = View.GONE
        binding.errorLayout.networkErrorLinearLayout.visibility = View.VISIBLE
    }

    private fun hideErrorLayout() {
        if (binding.errorLayout.networkErrorLinearLayout.visibility == View.GONE) return
        binding.errorLayout.networkErrorLinearLayout.visibility = View.GONE
        binding.searchedPlacesRecyclerView.visibility = View.VISIBLE
    }

    /*private fun searchByCoordinates(input: String) {
        MapUtil.stringCoordinatesToGeoPoint(input).let {
            it ?: return

            genericRecyclerViewAdapter.updateContent(
                listOf(
                    SearchedPlace(
                        name = resources.getString(R.string.split_two_strings_formatter)
                            .format(
                                MapUtil.latitudeToDMS(it.latitude),
                                MapUtil.longitudeToDMS(it.longitude)
                            ),
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                )
            )
        }
    }*/

    private fun populateRecyclerView(places: UiState<SearchedPlaceUiModel>) {
        when (val exception = places.error) {

            is IOException -> {
                binding.errorLayout.apply {
                    errorImageView.apply {
                        setImageResource(
                            if (exception is ConnectException) R.drawable.service_unavailable
                            else R.drawable.network_error
                        )
                    }
                    errorTextView.text = resources.getString(
                        if (exception is ConnectException) R.string.service_unavailable
                        else R.string.network_error

                    )
                }

                showErrorLayout()
            }

            null -> {
                if (binding.searchedPlacesRecyclerView.visibility != View.VISIBLE) {
                    binding.errorLayout.networkErrorLinearLayout.visibility = View.GONE
                    binding.searchedPlacesRecyclerView.visibility = View.VISIBLE
                }

                places.items.let { searchedPlaces ->
                    /*if (searchedPlaces.isEmpty()) {
                        if (MapUtil.areCoordinatesValid(binding.searchMapBox.searchMapEditText.text.toString())) {
                            searchByCoordinates(binding.searchMapBox.searchMapEditText.toString())
                        }
                        return@let
                    } */

                    // don't sort list if it contains single element
                    if (searchedPlaces.size > 1) {
                        viewModel.sortListByDistance(
                            searchedPlaces,
                            mapSharedViewModel.lastKnownPosition.replayCache.lastOrNull()
                        ).let { sortedList ->
                            genericRecyclerViewAdapter.updateContent(sortedList)
                        }
                    } else genericRecyclerViewAdapter.updateContent(searchedPlaces)
                }
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
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
                    R.color.md_theme_primaryContainer
                )
            )
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // reset state
        mapSharedViewModel.setDismissSearchMapDialogState(null)
    }
}