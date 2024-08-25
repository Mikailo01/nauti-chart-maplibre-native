package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoadingDialogSharedViewModel : ViewModel() {
    private val _progressSharedFlow = MutableSharedFlow<Int>()
    val progressSharedFlow: SharedFlow<Int> = _progressSharedFlow.asSharedFlow()

    fun updateProgress(progress: Int) {
        viewModelScope.launch {
            _progressSharedFlow.emit(progress)
        }
    }
}