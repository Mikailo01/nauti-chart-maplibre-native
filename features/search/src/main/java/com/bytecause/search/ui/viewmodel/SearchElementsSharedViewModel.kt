package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.model.ElementTagModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchElementsSharedViewModel : ViewModel() {

    private val _filteredTagsStateFlow = MutableStateFlow<Map<String, List<String>>?>(null)
    val filteredTagsStateFlow get() = _filteredTagsStateFlow.asStateFlow()

    fun setFilter(filterList: Map<String, List<String>>?) {
        _filteredTagsStateFlow.value = filterList
    }

    private val _allTagsSharedFlow =
        MutableSharedFlow<Map<String, List<ElementTagModel>>>(replay = 1)
    val allTagsSharedFlow get() = _allTagsSharedFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun resetTags() {
        _allTagsSharedFlow.resetReplayCache()
    }

    fun setTags(tagsMap: Map<String, List<ElementTagModel>>) {
        viewModelScope.launch {
            _allTagsSharedFlow.emit(tagsMap)
        }
    }
}