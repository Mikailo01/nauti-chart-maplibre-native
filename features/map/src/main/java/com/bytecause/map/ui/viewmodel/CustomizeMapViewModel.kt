package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.UiState
import com.bytecause.domain.model.VesselModel
import com.bytecause.domain.usecase.FetchHarboursUseCase
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
    private val fetchHarboursUseCase: FetchHarboursUseCase
) : ViewModel() {
    private val _vesselsFetchingState = MutableStateFlow<UiState<VesselModel>?>(null)
    val vesselsFetchingState = _vesselsFetchingState.asStateFlow()

    private val _harboursFetchingState = MutableStateFlow<UiState<HarboursModel>?>(null)
    val harboursFetchingState = _harboursFetchingState.asStateFlow()

    val getAllDistinctCategories: Flow<List<String>> = poiCacheRepository.getAllDistinctCategories()

    fun fetchVessels() {
        viewModelScope.launch {
            _vesselsFetchingState.emit(UiState(isLoading = true))
            when (val data = fetchVesselsUseCase().firstOrNull() ?: return@launch) {
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
            }
            _vesselsFetchingState.emit(UiState(isLoading = false))
        }
    }

    fun fetchHarbours() {
        viewModelScope.launch {
            _harboursFetchingState.emit(UiState(isLoading = true))

            when (val data = fetchHarboursUseCase().firstOrNull() ?: return@launch) {
                is ApiResult.Failure -> {
                    _harboursFetchingState.emit(
                        UiState(
                            isLoading = false,
                            error = data.exception
                        )
                    )
                }

                is ApiResult.Success -> {
                    _harboursFetchingState.emit(
                        UiState(
                            isLoading = false,
                            items = data.data ?: emptyList(),
                        ),
                    )
                }
            }

            _harboursFetchingState.emit(UiState(isLoading = false))
        }
    }
}
