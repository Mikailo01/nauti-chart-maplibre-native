package com.bytecause.nautichart.ui.view.fragment.dialog

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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.SelectedCategoryElementsFilterDialogLayoutBinding
import com.bytecause.nautichart.domain.model.ElementTagModel
import com.bytecause.nautichart.interfaces.SelectCheckBoxInterface
import com.bytecause.nautichart.ui.adapter.TagsFilterParentAdapter
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.CategoryElementsListFilterDialogViewModel
import com.bytecause.nautichart.ui.viewmodels.SearchElementsSharedViewModel
import com.bytecause.nautichart.util.Util
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class SelectedCategoryElementsListFilterDialog : DialogFragment(), SelectCheckBoxInterface {

    private val binding by viewBinding(SelectedCategoryElementsFilterDialogLayoutBinding::inflate)

    private val viewModel: CategoryElementsListFilterDialogViewModel by viewModels()
    private val sharedViewModel: SearchElementsSharedViewModel by activityViewModels()

    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var parentRecyclerViewAdapter: TagsFilterParentAdapter

    private val tagsMap = mutableMapOf<String, List<ElementTagModel>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clearFiltersButton.setOnClickListener {
            if (!Util().lastClick(1000)) {
                return@setOnClickListener
            }

            val updatedTagsMap = tagsMap.mapValues { (_, tagModels) ->
                tagModels.map { tagModel ->
                    tagModel.copy(isChecked = false)
                }.sortedWith(
                    compareBy { it.tagName.lowercase() }
                )
            }

            tagsMap.clear()
            tagsMap.putAll(updatedTagsMap)
            parentRecyclerViewAdapter.notifyItemRangeChanged(0, tagsMap.keys.size)
        }

        binding.btnDone.setOnClickListener {
            tagsMap.mapValues { (_, tagsList) ->
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                // This SharedFlow takes only the first emitted value and will ignore other emissions.
                sharedViewModel.allTagsSharedFlow.take(1).collect { allTags ->
                    if (viewModel.tagMap.isNotEmpty()) {
                        tagsMap.putAll(viewModel.tagMap)
                    }

                    else if (viewModel.tagMap.isEmpty()) {
                        tagsMap.putAll(
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

                    parentRecyclerView = binding.checkBoxParentRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        setHasFixedSize(true)
                        parentRecyclerViewAdapter = TagsFilterParentAdapter(
                            tagsMap,
                            this@SelectedCategoryElementsListFilterDialog
                        )
                        adapter = parentRecyclerViewAdapter
                        viewModel.recyclerViewExpandedStateList.takeIf { stateList -> stateList.isNotEmpty() }
                            ?.let { states ->
                                parentRecyclerViewAdapter.restoreExpandedStates(states)
                            }
                    }
                }
            }
        }
    }

    /*// Sort check box list alphabetically by name and then sort by isChecked state (true first).
    private fun sortCheckBoxes() {
        tagsList.sortedWith(
            compareBy { it.tagName.lowercase() }
        ).let { listSortedByName ->
            listSortedByName.sortedByDescending { element -> element.isChecked }
                .let { listSortedByState ->
                    tagsList.clear()
                    tagsList.addAll(listSortedByState)
                }
        }
    }*/

    override fun onStart() {
        super.onStart()
        // Apply the fullscreen dialog style
        val displayMetrics = resources.displayMetrics
        dialog?.window?.setLayout(
            (displayMetrics.widthPixels * 0.9).toInt(),
            if (displayMetrics.heightPixels > displayMetrics.widthPixels) displayMetrics.heightPixels / 2 else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_dialog)
        )
    }

    override fun onCheckBoxClickListener(
        buttonView: CompoundButton,
        position: Int,
        parentPosition: Int,
        isChecked: Boolean
    ) {
        val elementTag = ElementTagModel(buttonView.tag.toString(), isChecked)
        val mapKey = tagsMap.keys.elementAt(parentPosition)

        val elementList = tagsMap[mapKey]?.toMutableList()
        elementList?.find { it.tagName == elementTag.tagName }?.let {
            elementList.remove(it)
            elementList.add(elementTag)
        }

        elementList?.sortedWith(
            compareBy { it.tagName.lowercase() }
        ).let { listSortedByName ->
            listSortedByName?.sortedByDescending { element -> element.isChecked }?.let {
                tagsMap[mapKey] = it
                parentRecyclerViewAdapter.notifyItemRangeChanged(0, tagsMap.keys.size)
            }
        }
    }

    override fun onDestroyView() {
        viewModel.saveRecyclerViewExpandedStates(parentRecyclerViewAdapter.getExpandedStateList())
        viewModel.saveTagsMap(tagsMap)
        super.onDestroyView()
    }
}