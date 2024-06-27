package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.domain.model.ElementTagModel

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