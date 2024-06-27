package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.UiState
import com.bytecause.domain.model.VesselModel
import com.bytecause.domain.usecase.FetchVesselsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeMapViewModel
@Inject
constructor(
    poiCacheRepository: PoiCacheRepository,
    private val fetchVesselsUseCase: FetchVesselsUseCase,
) : ViewModel() {
    private val _vesselsFetchingState = MutableStateFlow<UiState<VesselModel>?>(null)
    val vesselsFetchingState get() = _vesselsFetchingState.asStateFlow()

    val getAllDistinctCategories: Flow<List<String>> = poiCacheRepository.getAllDistinctCategories()

    fun fetchVessels() {
        viewModelScope.launch {
            _vesselsFetchingState.value = UiState(isLoading = true)
            when (val data = fetchVesselsUseCase().firstOrNull()) {
                is ApiResult.Success -> {
                    _vesselsFetchingState.emit(
                        UiState(
                            isLoading = false,
                            items = data.data ?: emptyList(),
                        ),
                    )
                }

                is ApiResult.Failure -> {
                    _vesselsFetchingState.emit(
                        UiState(
                            isLoading = false,
                            error = data.exception
                        )
                    )
                }

                else -> {
                    return@launch
                }
            }
            _vesselsFetchingState.value = _vesselsFetchingState.value?.copy(isLoading = false)
        }
    }
}
