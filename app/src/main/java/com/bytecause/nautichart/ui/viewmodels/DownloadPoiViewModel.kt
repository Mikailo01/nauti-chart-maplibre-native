package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.local.room.tables.Continent
import com.bytecause.nautichart.data.repository.ContinentDatabaseRepository
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
    continentDatabaseRepository: ContinentDatabaseRepository
) : ViewModel() {

    private val getAllContinents: Flow<List<Continent>> = continentDatabaseRepository.getAllContinents

    private val _continentListStateFlow = MutableStateFlow<List<Continent>?>(null)
    val continentListStateFlow: StateFlow<List<Continent>?> get() = _continentListStateFlow.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAllContinents.firstOrNull()?.let { continentList ->
                _continentListStateFlow.emit(continentList.sortedBy { it.name })
            }
        }
    }

}