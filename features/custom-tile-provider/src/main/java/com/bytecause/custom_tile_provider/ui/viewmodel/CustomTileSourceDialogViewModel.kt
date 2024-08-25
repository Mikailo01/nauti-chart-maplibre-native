package com.bytecause.custom_tile_provider.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.custom_tile_provider.data.repository.abstractions.GetTileImageRepository
import com.bytecause.custom_tile_provider.ui.events.CustomTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.CustomTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.CustomTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
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
class CustomTileSourceDialogViewModel @Inject constructor(
    private val customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
    private val customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
    private val customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository,
    private val getTileImageRepository: GetTileImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomTileSourceState())
    val uiState: StateFlow<CustomTileSourceState> = _uiState.asStateFlow()

    private val _effect = Channel<CustomTileSourceEffect>(capacity = Channel.CONFLATED)
    val effect: Flow<CustomTileSourceEffect> = _effect.receiveAsFlow()

    fun uiEventHandler(event: CustomTileSourceEvent) {
        when (event) {
            is CustomTileSourceEvent.OnLoadingValueChange -> onLoadingValueChange(event.value)
            is CustomTileSourceEvent.OnSourceNameChange -> onSourceNameChange(event.value)
            is CustomTileSourceEvent.OnTileSizeValueChange -> onTileSizeValueChange(event.value)
            is CustomTileSourceEvent.OnUrlValidationChange -> onUrlValidationChange(event.value)
            is CustomTileSourceEvent.OnUrlValueChange -> onUrlValueChange(event.value)
            is CustomTileSourceEvent.OnTabClick -> sendEffect(CustomTileSourceEffect.TabClick(event.value))
            is CustomTileSourceEvent.OnSourceNameError -> onSourceNameError(event.error)
            CustomTileSourceEvent.OnDoneButtonClick -> sendEffect(CustomTileSourceEffect.DoneButtonClick)
            CustomTileSourceEvent.OnLaunchFileManager -> sendEffect(CustomTileSourceEffect.LaunchFileManager)
            CustomTileSourceEvent.OnNavigateBack -> sendEffect(CustomTileSourceEffect.NavigateBack)
            CustomTileSourceEvent.OnToggleRangeSliderVisibility -> sendEffect(CustomTileSourceEffect.ToggleRangeSliderVisibility)
            CustomTileSourceEvent.OnTileSizeNotSelected -> sendEffect(CustomTileSourceEffect.TileSizeNotSelected)
        }
    }

    private fun sendEffect(effect: CustomTileSourceEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun onSourceNameError(error: TileNameError) {
        _uiState.update {
            it.copy(sourceNameError = error)
        }
    }

    private fun onLoadingValueChange(boolean: Boolean) {
        _uiState.update {
            it.copy(isLoading = boolean)
        }
    }

    private fun onSourceNameChange(value: String) {
        _uiState.update {
            it.copy(
                sourceName = value.trimIndent(),
                sourceNameError = null
            )
        }
    }

    private fun onTileSizeValueChange(value: Int) {
        _uiState.update {
            it.copy(tileSize = value)
        }
    }

    private fun onUrlValidationChange(boolean: Boolean) {
        _uiState.update {
            it.copy(isUrlValid = boolean)
        }
    }

    private fun onUrlValueChange(value: String) {
        _uiState.update {
            it.copy(urlValue = value.trimIndent())
        }
    }

    fun saveOfflineRasterTileProvider(tileProvider: CustomTileProvider) {
        viewModelScope.launch {
            customOfflineRasterTileSourceRepository.saveOfflineRasterTileSourceProvider(tileProvider)
        }
    }

    fun saveOnlineRasterTileProvider(tileProvider: CustomTileProvider) {
        viewModelScope.launch {
            customOnlineRasterTileSourceRepository.saveOnlineRasterTileSourceProvider(tileProvider)
        }
    }

    fun saveOfflineVectorTileProvider(tileProvider: CustomTileProvider) {
        viewModelScope.launch {
            customOfflineVectorTileSourceRepository.saveOfflineVectorTileSourceProvider(tileProvider)
        }
    }

    suspend fun getTileImage(url: String): ByteArray? = getTileImageRepository.getImage(url)
}