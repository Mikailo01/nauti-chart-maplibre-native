package com.bytecause.pois.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.model.ContinentModel
import com.bytecause.pois.data.repository.abstractions.ContinentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadPoiViewModel @Inject constructor(
    continentRepository: ContinentRepository
) : ViewModel() {

    private val getAllContinents: Flow<List<ContinentModel>> = continentRepository.getAllContinents()

    private val _continentListStateFlow = MutableStateFlow<List<ContinentModel>?>(null)
    val continentListStateFlow: StateFlow<List<ContinentModel>?> get() = _continentListStateFlow.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAllContinents.firstOrNull()?.let { continentList ->
                _continentListStateFlow.emit(continentList.sortedBy { it.name })
            }
        }
    }

}