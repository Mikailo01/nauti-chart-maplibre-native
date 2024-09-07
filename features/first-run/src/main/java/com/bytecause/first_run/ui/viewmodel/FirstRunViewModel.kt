package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.UiState
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.domain.util.SearchTypes
import com.bytecause.util.poi.PoiUtil.excludeAmenityObjectsFilterList
import com.bytecause.util.poi.PoiUtil.searchTypesStringList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel
@Inject
constructor(
    private val getPoiResultByRegionUseCase: GetPoiResultByRegionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiStateFlow = MutableStateFlow<UiState<String>?>(null)
    val uiStateFlow get() = _uiStateFlow.asStateFlow()

    private var downloadJob: Job? = null

    var region: String? = null
        private set

    fun resetUiState() {
        _uiStateFlow.value = null
    }

    fun cancelDownloadJob() {
        downloadJob?.cancel()
        downloadJob = null
    }

    fun setRegion(region: String) {
        this.region = region
    }

    fun getPoiResult() {
        region?.let { regionName ->
            downloadJob =
                viewModelScope.launch {
                    _uiStateFlow.value = UiState(loading = Loading(true))

                    val query = OverpassQueryBuilder
                        .format(OverpassQueryBuilder.FormatTypes.JSON)
                        .timeout(240)
                        .region(regionName)
                        .type(OverpassQueryBuilder.Type.Node)
                        .search(
                            SearchTypes.UnionSet(searchTypesStringList)
                                .filterNot(
                                    emptyList(),
                                    excludeAmenityObjectsFilterList,
                                    emptyList(),
                                    emptyList(),
                                    emptyList(),
                                    emptyList()
                                )
                        )
                        .build()

                    getPoiResultByRegionUseCase(query = query).collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                _uiStateFlow.emit(
                                    UiState(
                                        loading = Loading(false),
                                        items = listOf(result.data ?: ""),
                                    ),
                                )
                            }

                            is ApiResult.Failure -> {
                                when (result.exception) {
                                    is ConnectException -> {
                                        _uiStateFlow.emit(UiState(error = ConnectException()))
                                    }

                                    is FileNotFoundException -> {
                                        _uiStateFlow.emit(UiState(error = FileNotFoundException()))
                                    }

                                    else -> {
                                        _uiStateFlow.emit(UiState(error = IOException()))
                                    }
                                }
                            }

                            is ApiResult.Progress -> {
                                result.progress?.let { progress ->
                                    _uiStateFlow.emit(
                                        UiState(
                                            loading = Loading(
                                                true,
                                                progress = uiStateFlow.value?.loading?.progress?.plus(
                                                    progress
                                                )
                                                    ?: progress
                                            )
                                        )
                                    )
                                }
                            }

                            else -> _uiStateFlow.emit(UiState(loading = Loading(false)))
                        }
                    }
                }
        }
    }

    fun saveFirstRunFlag(flag: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveFirstRunFlag(flag)
        }
    }
}
