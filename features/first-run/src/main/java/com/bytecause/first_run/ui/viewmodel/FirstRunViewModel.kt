package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.CountryRepository
import com.bytecause.data.services.communication.ServiceApiResultListener
import com.bytecause.data.services.communication.ServiceEvent
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.RegionModel
import com.bytecause.domain.usecase.GetRegionsUseCase
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.first_run.ui.Region
import com.bytecause.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel
@Inject
constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getRegionsUseCase: GetRegionsUseCase,
    private val countryRepository: CountryRepository
) : ViewModel() {

    private val _downloadPoiUiState = MutableStateFlow<UiState<Nothing>?>(null)
    val downloadPoiUiState = _downloadPoiUiState.asStateFlow()

    private val _downloadRegionsUiState = MutableStateFlow<UiState<RegionModel>?>(null)
    val downloadRegionsUiState = _downloadRegionsUiState.asStateFlow()

    var region: Region? = null
        private set

    fun setRegion(region: Region) {
        this.region = region
    }

    init {
        viewModelScope.launch {
            ServiceApiResultListener.eventFlow.collect { event ->
                when (event) {
                    is ServiceEvent.RegionPoiDownloadStarted -> {
                        _downloadPoiUiState.emit(
                            UiState(
                                loading = Loading(true)
                            )
                        )
                    }

                    is ServiceEvent.RegionPoiDownload -> {
                        when (val result = event.result) {
                            is ApiResult.Failure -> {
                                _downloadPoiUiState.emit(UiState(error = result.exception))
                            }

                            is ApiResult.Progress -> {
                                result.progress?.let { progress ->
                                    _downloadPoiUiState.emit(
                                        UiState(
                                            loading = Loading(
                                                isLoading = true,
                                                progress = progress
                                            )
                                        )
                                    )
                                }
                            }

                            is ApiResult.Success -> {
                                _downloadPoiUiState.emit(
                                    UiState(
                                        loading = Loading(false)
                                    )
                                )
                            }
                        }
                    }

                    is ServiceEvent.RegionPoiDownloadCancelled -> {
                        _downloadPoiUiState.emit(null)
                    }

                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    fun getRegions(isoCode: String) {
        viewModelScope.launch {
            val countryId = countryRepository.getCountryByIso(isoCode).first().id

            val query = OverpassQueryBuilder
                .format(OverpassQueryBuilder.FormatTypes.JSON)
                .timeout(120)
                .geocodeAreaISO(isoCode)
                .type(OverpassQueryBuilder.Type.Relation)
                .adminLevel(4)
                .build()

            _downloadRegionsUiState.value = UiState(loading = Loading(true))

            when (val result = getRegionsUseCase(countryId, isoCode, query).firstOrNull()) {
                is ApiResult.Failure -> {
                    _downloadRegionsUiState.emit(UiState(error = result.exception))
                }

                is ApiResult.Progress -> {
                    // nothing
                }

                is ApiResult.Success -> {
                    _downloadRegionsUiState.emit(UiState(items = result.data ?: emptyList()))
                }

                else -> _downloadRegionsUiState.value = UiState(loading = Loading(false))
            }
        }
    }

    suspend fun saveFirstRunFlag(flag: Boolean) {
        userPreferencesRepository.saveFirstRunFlag(flag)
    }
}
