package com.bytecause.custom_tile_provider.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.custom_tile_provider.ui.events.OnlineTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.OnlineTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.OnlineTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.custom_tile_provider.util.AnalyzeCustomOnlineTileProvider.extractTileUrlAttrs
import com.bytecause.custom_tile_provider.util.Util.formatTileUrlForRaster
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineTileSourceViewModel @Inject constructor(
    private val customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineTileSourceState())
    val uiState: StateFlow<OnlineTileSourceState> =
        _uiState.asStateFlow()

    private val _effect = Channel<OnlineTileSourceEffect>(capacity = Channel.CONFLATED)
    val effect: Flow<OnlineTileSourceEffect> = _effect.receiveAsFlow()

    fun uiEventHandler(event: OnlineTileSourceEvent) {
        when (event) {
            is OnlineTileSourceEvent.OnLoadingValueChange -> {
                _uiState.update {
                    it.copy(isLoading = event.value)
                }
            }

            is OnlineTileSourceEvent.OnSourceNameChange -> {
                _uiState.update {
                    it.copy(
                        sourceName = event.value.trimIndent(),
                        sourceNameError = null
                    )
                }
            }

            is OnlineTileSourceEvent.OnTileSizeValueChange -> {
                _uiState.update {
                    it.copy(tileSize = event.value)
                }
            }

            is OnlineTileSourceEvent.OnUrlValidationChange -> {
                _uiState.update {
                    it.copy(isUrlValid = event.value)
                }
            }

            is OnlineTileSourceEvent.OnUrlValueChange -> {
                _uiState.update {
                    it.copy(urlValue = event.value.trimIndent())
                }
            }

            is OnlineTileSourceEvent.OnTabClick -> sendEffect(OnlineTileSourceEffect.TabClick(event.value))
            is OnlineTileSourceEvent.OnSourceNameError -> {
                _uiState.update {
                    it.copy(sourceNameError = event.error)
                }
            }

            is OnlineTileSourceEvent.OnDoneButtonClick -> {

                if (checkAllPropsValid()) {
                    extractTileUrlAttrs(
                        // Url shouldn't contain any whitespaces
                        uiState.value.urlValue.takeIf { !it.contains(" ") } ?: run {
                            uiEventHandler(
                                OnlineTileSourceEvent.OnUrlValueChange(
                                    uiState.value.urlValue.trimIndent()
                                )
                            )
                            uiState.value.urlValue
                        }
                    )?.let { tileUrlInfo ->
                        // TODO("Add support for online vector tile provider.")
                        viewModelScope.launch {
                            saveOnlineRasterTileProvider(
                                CustomTileProvider(
                                    CustomTileProviderType.Raster.Online(
                                        name = uiState.value.sourceName,
                                        url = tileUrlInfo.url,
                                        tileFileFormat = tileUrlInfo.tileFileFormat,
                                        minZoom = event.minZoom,
                                        maxZoom = event.maxZoom,
                                        tileSize = uiState.value.tileSize,
                                        imageUrl = formatTileUrlForRaster(tileUrlInfo.url)
                                    )
                                )
                            )

                            sendEffect(OnlineTileSourceEffect.NavigateBack)
                        }
                    } ?: run {
                        uiEventHandler(
                            OnlineTileSourceEvent.OnUrlValidationChange(
                                false
                            )
                        )
                    }
                }

            }

            OnlineTileSourceEvent.OnNavigateBack -> sendEffect(OnlineTileSourceEffect.NavigateBack)
            OnlineTileSourceEvent.OnToggleRangeSliderVisibility -> sendEffect(OnlineTileSourceEffect.ToggleRangeSliderVisibility)
            OnlineTileSourceEvent.OnTileSizeNotSelected -> sendEffect(OnlineTileSourceEffect.TileSizeNotSelected)
        }
    }

    private fun checkAllPropsValid(): Boolean {
        return if (uiState.value.sourceName.isBlank() || uiState.value.urlValue.isBlank() || uiState.value.tileSize == -1) {
            if (uiState.value.sourceName.isBlank()) {
                uiEventHandler(
                    OnlineTileSourceEvent.OnSourceNameError(
                        TileNameError.Empty
                    )
                )
            }

            if (uiState.value.urlValue.isBlank()) {
                uiEventHandler(
                    OnlineTileSourceEvent.OnUrlValidationChange(
                        false
                    )
                )
            }

            if (uiState.value.tileSize == -1) {
                uiEventHandler(OnlineTileSourceEvent.OnTileSizeNotSelected)
            }

            false
        } else true
    }

    private fun sendEffect(effect: OnlineTileSourceEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private suspend fun saveOnlineRasterTileProvider(tileProvider: CustomTileProvider) {
        customOnlineRasterTileSourceRepository.saveOnlineRasterTileSourceProvider(tileProvider)
    }
}