package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.search.ui.model.ElementTagModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryElementsListFilterDialogViewModel : ViewModel() {

    var recyclerViewExpandedStateList = emptyList<Boolean>()
        private set

    private var _tagsMap: MutableStateFlow<Map<String, List<ElementTagModel>>> = MutableStateFlow(
        emptyMap()
    )
    val tagsMap: StateFlow<Map<String, List<ElementTagModel>>> = _tagsMap.asStateFlow()

    fun saveTags(tags: Map<String, List<ElementTagModel>>) {
        viewModelScope.launch {
            _tagsMap.emit(tags)
        }
    }

    fun clearFilters() {
        val updatedTagsMap = tagsMap.value.mapValues { (_, tagModels) ->
            tagModels.map { tagModel ->
                tagModel.copy(isChecked = false)
            }.sortedWith(
                compareBy { it.tagName.lowercase() }
            )
        }

        saveTags(updatedTagsMap)
    }

    fun checkBoxClicked(element: ElementTagModel, parentPosition: Int) {
        val mapKey = tagsMap.value.keys.elementAt(parentPosition)

        val elementList = tagsMap.value[mapKey]?.toMutableList()
        elementList?.find { it.tagName == element.tagName }?.let {
            elementList.remove(it)
            elementList.add(element)
        }

        elementList?.sortedWith(
            compareByDescending<ElementTagModel> { it.isChecked }
                .thenBy { it.tagName.lowercase() }
        )?.let {
            val updatedTags = tagsMap.value.toMutableMap().apply {
                this[mapKey] = it
            }
            saveTags(updatedTags)
        }
    }

    fun saveRecyclerViewExpandedStates(stateList: List<Boolean>) {
        recyclerViewExpandedStateList = stateList
    }
}