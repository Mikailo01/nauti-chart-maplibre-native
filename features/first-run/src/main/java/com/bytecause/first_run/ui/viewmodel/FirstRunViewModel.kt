package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.UiState
import com.bytecause.domain.usecase.GetPoiResultByRadiusUseCase
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.util.string.StringUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel
    @Inject
    constructor(
        private val getPoiResultByRadiusUseCase: GetPoiResultByRadiusUseCase,
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

        /*fun getPoiResult(regionName: String) {
            downloadJob =
                viewModelScope.launch {
                    _uiStateFlow.value = UiState(isLoading = true)

                    val query =
                        SimpleOverpassQueryBuilder(
                            format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                            timeoutInSeconds = 120,
                            regionNameList = listOf(regionName),
                            type = "node",
                            search = SearchTypes.UnionSet(StringUtil.searchTypesStringList.toTypedArray()),
                        ).getQuery()

                    when (val result = getPoiResultByRegionUseCase(regionName, query).firstOrNull()) {
                        is ApiResult.Success -> {
                            _uiStateFlow.emit(
                                UiState(
                                    isLoading = false,
                                    items = listOf(result.data ?: ""),
                                ),
                            )
                        }

                        is ApiResult.Failure -> {
                            when (result.exception) {
                                is ConnectException -> {
                                    _uiStateFlow.emit(UiState(error = UiState.Error.ServiceUnavailable))
                                }

                                is FileNotFoundException -> {
                                    _uiStateFlow.emit(UiState(error = UiState.Error.Other))
                                }

                                else -> {
                                    _uiStateFlow.emit(UiState(error = UiState.Error.NetworkError))
                                }
                            }
                        }

                        else -> _uiStateFlow.emit(UiState(error = UiState.Error.NetworkError))
                    }
                }
        }*/

        fun saveFirstRunFlag(flag: Boolean) {
            viewModelScope.launch {
                userPreferencesRepository.saveFirstRunFlag(flag)
            }
        }

        // generates string from multiple words separated '_', this is needed for removing duplicates from
        // Overpass API query, e.x.: amenity!shop!seamark:type!tourism, query will search for all objects
        // with "amenity" tag key in region and exclude all elements appended after '!', this is necessary
        // because some objects share same tag keys.
    /*fun generateObjectList(): Array<String> {
        for (x in stringList.indices) {
            val stringBuilder = StringBuilder()
            stringBuilder.append(stringList[x])
            for (y in stringList.indices) {
                if (y != x) stringBuilder.append("!" + stringList[y])
            }
            stringArray.add(stringBuilder.toString())
        }
        return stringArray.toTypedArray()
    }*/
    }
