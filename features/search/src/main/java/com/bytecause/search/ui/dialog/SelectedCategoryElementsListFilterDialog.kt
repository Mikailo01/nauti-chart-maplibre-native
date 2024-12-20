package com.bytecause.search.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.search.ui.model.ElementTagModel
import com.bytecause.features.search.databinding.SelectedCategoryElementsFilterDialogLayoutBinding
import com.bytecause.search.ui.recyclerview.adapter.TagsFilterParentAdapter
import com.bytecause.search.ui.recyclerview.interfaces.SelectCheckBoxListener
import com.bytecause.search.ui.viewmodel.CategoryElementsListFilterDialogViewModel
import com.bytecause.search.ui.viewmodel.SearchElementsSharedViewModel
import com.bytecause.util.common.LastClick
import com.bytecause.util.delegates.viewBinding
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class SelectedCategoryElementsListFilterDialog : DialogFragment(), SelectCheckBoxListener {

    private val binding by viewBinding(
        SelectedCategoryElementsFilterDialogLayoutBinding::inflate
    )

    private val viewModel: CategoryElementsListFilterDialogViewModel by viewModels()
    private val sharedViewModel: SearchElementsSharedViewModel by activityViewModels()

    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var parentRecyclerViewAdapter: TagsFilterParentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentRecyclerView = binding.checkBoxParentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            parentRecyclerViewAdapter = TagsFilterParentAdapter(
                emptyMap(),
                this@SelectedCategoryElementsListFilterDialog
            )
            adapter = parentRecyclerViewAdapter
            viewModel.recyclerViewExpandedStateList.takeIf { stateList -> stateList.isNotEmpty() }
                ?.let { states ->
                    parentRecyclerViewAdapter.restoreExpandedStates(states)
                }
        }

        binding.clearFiltersButton.setOnClickListener {
            if (!LastClick().lastClick(1000)) {
                return@setOnClickListener
            }
            viewModel.clearFilters()
        }

        binding.btnDone.setOnClickListener {
            viewModel.tagsMap.value.mapValues { (_, tagsList) ->
                tagsList.filter { it.isChecked }
            }.filterValues { it.isNotEmpty() }.let { tagList ->

                val filterMap = mutableMapOf<String, MutableList<String>>()

                for ((key, value) in tagList) {
                    for (element in value) {
                        if (filterMap.containsKey(key)) filterMap[key]?.add(element.tagName)
                        else filterMap[key] = mutableListOf(element.tagName)
                    }
                }

                if (sharedViewModel.filteredTagsStateFlow.value != filterMap) sharedViewModel.setFilter(
                    filterMap
                )
            }
            dismiss()
        }

        this@SelectedCategoryElementsListFilterDialog.isCancelable = false

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This SharedFlow takes only the first emitted value and will ignore other emissions.
                sharedViewModel.allTagsSharedFlow.take(1).collect { allTags ->

                    if (viewModel.tagsMap.value.isEmpty()) {
                        viewModel.saveTags(
                            allTags.map { mapEntry ->
                                mapEntry.value.sortedWith(
                                    compareBy { listElement -> listElement.tagName.lowercase() }
                                ).let { listSortedByName ->
                                    listSortedByName.sortedByDescending { listElement -> listElement.isChecked }
                                        .let { sortedList ->
                                            mapEntry.key to sortedList
                                        }
                                }
                            }.toMap()
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagsMap.collect { tagsMap ->
                    parentRecyclerViewAdapter.submitMap(tagsMap)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Apply the fullscreen dialog style
        val displayMetrics = resources.displayMetrics
        dialog?.window?.setLayout(
            (displayMetrics.widthPixels * 0.9).toInt(),
            if (displayMetrics.heightPixels > displayMetrics.widthPixels) displayMetrics.heightPixels / 2
            else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                com.bytecause.core.resources.R.drawable.rounded_dialog
            )
        )
    }

    override fun onCheckBoxClickListener(
        buttonView: CompoundButton,
        position: Int,
        parentPosition: Int,
        isChecked: Boolean
    ) {
        val elementTag = ElementTagModel(tagName = buttonView.tag.toString(), isChecked = isChecked)
        viewModel.checkBoxClicked(element = elementTag, parentPosition = parentPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.saveRecyclerViewExpandedStates(parentRecyclerViewAdapter.getExpandedStateList())
    }
}