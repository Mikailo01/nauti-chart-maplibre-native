package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomMarkerCategorySharedViewModel: ViewModel() {

    private val _drawableIdStateFlow = MutableStateFlow(-1)
    val drawableIdStateFlow: StateFlow<Int> = _drawableIdStateFlow.asStateFlow()

    fun setDrawableId(id: Int) {
        _drawableIdStateFlow.value = id
    }

    fun resetDrawableId() {
        _drawableIdStateFlow.value = -1
    }
}
