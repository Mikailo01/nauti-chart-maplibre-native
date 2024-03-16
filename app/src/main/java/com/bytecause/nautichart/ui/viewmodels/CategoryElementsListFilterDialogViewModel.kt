package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.bytecause.nautichart.domain.model.ElementTagModel

class CategoryElementsListFilterDialogViewModel: ViewModel() {

    var recyclerViewExpandedStateList = listOf<Boolean>()
        private set

    var tagMap = mapOf<String, List<ElementTagModel>>()
        private set

    fun saveRecyclerViewExpandedStates(stateList: List<Boolean>) {
        recyclerViewExpandedStateList = stateList
    }

    fun saveTagsMap(tagMap: Map<String, List<ElementTagModel>>) {
        this.tagMap = tagMap
    }
}