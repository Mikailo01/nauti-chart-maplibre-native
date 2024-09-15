package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.model.RegionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FirstRunSharedViewModel : ViewModel() {

    private val _regionsSharedFlow = MutableStateFlow<List<RegionModel>>(emptyList())
    val regionsSharedFlow: StateFlow<List<RegionModel>> = _regionsSharedFlow.asStateFlow()

    private val _selectedRegion = MutableStateFlow<RegionModel?>(null)
    val selectedRegion: StateFlow<RegionModel?> = _selectedRegion.asStateFlow()

    fun setRegions(regions: List<RegionModel>) {
        _regionsSharedFlow.update { regions }
    }

    fun setSelectedRegion(region: RegionModel) {
        viewModelScope.launch {
            _selectedRegion.emit(region)
        }
    }
}