package com.bytecause.pois.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.model.ContinentModel
import com.bytecause.pois.data.repository.abstractions.ContinentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DownloadPoiViewModel @Inject constructor(
    continentRepository: ContinentRepository
) : ViewModel() {

    val getAllContinents: Flow<List<ContinentModel>> =
        continentRepository.getAllContinents()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}