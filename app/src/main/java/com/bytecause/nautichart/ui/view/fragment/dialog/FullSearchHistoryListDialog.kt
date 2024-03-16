package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.RecentlySearchedPlace
import com.bytecause.nautichart.databinding.FullSearchHistoryListDialogBinding
import com.bytecause.nautichart.interfaces.SearchHistoryRecyclerViewInterface
import com.bytecause.nautichart.ui.adapter.FullSearchHistoryListParentAdapter
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.FullSearchHistoryListDialogViewModel
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FullSearchHistoryListDialog : DialogFragment(R.layout.full_search_history_list_dialog),
    SearchHistoryRecyclerViewInterface {

    private val binding by viewBinding(FullSearchHistoryListDialogBinding::bind)

    private val viewModel: FullSearchHistoryListDialogViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var parentRecyclerViewAdapter: FullSearchHistoryListParentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadSearchHistory(
            viewModel.getRecentlySearchedPlaceList,
            resources.getStringArray(R.array.searchHistoryKeys)
        )

        binding.toolbar.apply {
            navBack.setOnClickListener {
                findNavController().popBackStack()
            }
            destNameTextView.text = getString(R.string.search_history_list)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.parentList.collect { content ->

                    if (!::parentRecyclerView.isInitialized) {
                        parentRecyclerView = binding.historyListParentRecyclerView.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            setHasFixedSize(true)
                            parentRecyclerViewAdapter =
                                FullSearchHistoryListParentAdapter(
                                    parentList = content,
                                    this@FullSearchHistoryListDialog
                                )
                            adapter = parentRecyclerViewAdapter
                        }
                    } else parentRecyclerViewAdapter.submitList(content)
                }
            }
        }
    }

    override fun onItemViewClickListener(view: View, parentIndex: Int, childIndex: Int) {
        viewModel.parentList.value[parentIndex].searchHistory[childIndex].let { recentlySearchedPlace ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                viewModel.updateRecentlySearchedPlaces(
                    element = RecentlySearchedPlace.newBuilder()
                        .setPlaceId(recentlySearchedPlace.placeId)
                        .setLatitude(recentlySearchedPlace.latitude)
                        .setLongitude(recentlySearchedPlace.longitude)
                        .setName(recentlySearchedPlace.name)
                        .setDisplayName(recentlySearchedPlace.displayName)
                        .setType(recentlySearchedPlace.type)
                        .setTimeStamp(System.currentTimeMillis())
                        .build(),
                    cache = viewModel.getRecentlySearchedPlaceList
                ).firstOrNull()?.let {
                    viewModel.updateRecentlySearchedPlaces(it)
                }

                mapSharedViewModel.apply {
                    // Make query to get needed element using AppSearch API.
                    viewModel.searchCachedResult(recentlySearchedPlace.name.takeIf { it.isNotEmpty() }
                        ?: recentlySearchedPlace.displayName).first {
                        it.placeId.toLong() == recentlySearchedPlace.placeId
                    }.let {
                        mapSharedViewModel.setPlaceToFind(it)
                    }
                    setDismissSearchMapDialogState(true)
                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
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