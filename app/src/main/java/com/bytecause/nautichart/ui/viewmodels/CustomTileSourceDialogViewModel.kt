package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.CustomOfflineTileSource
import com.bytecause.nautichart.CustomOnlineTileSource
import com.bytecause.nautichart.data.repository.CustomOfflineTileSourceRepositoryImpl
import com.bytecause.nautichart.data.repository.CustomOnlineTileSourceRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomTileSourceDialogViewModel @Inject constructor(
    private val customOfflineTileSourceRepository: CustomOfflineTileSourceRepositoryImpl,
    private val customOnlineTileSourceRepository: CustomOnlineTileSourceRepositoryImpl
) : ViewModel() {

    fun saveOfflineTileProvider(tileProvider: CustomOfflineTileSource) {
        viewModelScope.launch {
            customOfflineTileSourceRepository.saveOfflineTileSourceProvider(tileProvider)
        }
    }

    fun saveOnlineTileProvider(tileProvider: CustomOnlineTileSource) {
        viewModelScope.launch {
            customOnlineTileSourceRepository.saveOnlineTileSourceProvider(tileProvider)
        }
    }
}