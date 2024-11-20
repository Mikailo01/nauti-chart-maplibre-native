package com.bytecause.custom_tile_provider.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.core.resources.R
import com.bytecause.custom_tile_provider.ui.events.OfflineTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.OfflineTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.OfflineTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.util.file.FileUtil.checkFilenameExists
import com.bytecause.util.file.FileUtil.copyFileToFolder
import com.bytecause.util.file.FileUtil.deleteFileFromFolder
import com.bytecause.util.file.FileUtil.offlineTilesDir
import com.bytecause.util.map.MbTileType
import com.bytecause.util.map.MbTilesLoader
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
class OfflineTileSourceViewModel @Inject constructor(
    application: Application,
    private val customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
    private val customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OfflineTileSourceState())
    val uiState: StateFlow<OfflineTileSourceState> =
        _uiState.asStateFlow()

    private val _effect = Channel<OfflineTileSourceEffect>(capacity = Channel.CONFLATED)
    val effect: Flow<OfflineTileSourceEffect> = _effect.receiveAsFlow()

    fun uiEventHandler(event: OfflineTileSourceEvent) {
        when (event) {
            is OfflineTileSourceEvent.OnLoadingValueChange -> {
                _uiState.update {
                    it.copy(isLoading = event.value)
                }
            }

            is OfflineTileSourceEvent.OnSourceNameChange -> {
                _uiState.update {
                    it.copy(
                        sourceName = event.value.trimIndent(),
                        sourceNameError = null
                    )
                }
            }

            is OfflineTileSourceEvent.OnTileSizeValueChange -> {
                _uiState.update {
                    it.copy(tileSize = event.value)
                }
            }

            is OfflineTileSourceEvent.OnTabClick -> sendEffect(
                OfflineTileSourceEffect.TabClick(
                    event.value
                )
            )

            is OfflineTileSourceEvent.OnSourceNameError -> {
                _uiState.update {
                    it.copy(sourceNameError = event.error)
                }
            }

            is OfflineTileSourceEvent.OnFileUriSelected -> {
                _uiState.update {
                    it.copy(fileUri = event.value)
                }
            }

            OfflineTileSourceEvent.OnDoneButtonClick -> {
                if (checkPropsSet()) {
                    performTileSourceCopy(false)
                }
            }

            OfflineTileSourceEvent.OnLaunchFileManager -> sendEffect(OfflineTileSourceEffect.LaunchFileManager)
            OfflineTileSourceEvent.OnNavigateBack -> sendEffect(OfflineTileSourceEffect.NavigateBack)
            OfflineTileSourceEvent.OnTileSizeNotSelected -> sendEffect(OfflineTileSourceEffect.TileSizeNotSelected)
            OfflineTileSourceEvent.OnVectorUnsupported -> {
                _uiState.update {
                    it.copy(isLoading = false)
                }
                sendEffect(OfflineTileSourceEffect.VectorUnsupported)
            }

            OfflineTileSourceEvent.OnTileSourceOverwriteDialogConfirm -> {
                _uiState.update { it.copy(sourceNameError = null) }
                performTileSourceCopy(true)
            }
            OfflineTileSourceEvent.OnTileSourceOverwriteDialogDismiss -> {
                _uiState.update { it.copy(sourceNameError = null) }
            }
        }
    }

    private fun checkPropsSet(): Boolean {
        if (uiState.value.tileSize == -1 || uiState.value.sourceName.isBlank()) {
            if (uiState.value.sourceName.isBlank()) {
                uiEventHandler(
                    OfflineTileSourceEvent.OnSourceNameError(TileNameError.Empty)
                )
            }

            if (uiState.value.tileSize == -1) {
                uiEventHandler(
                    OfflineTileSourceEvent.OnTileSizeNotSelected
                )
            }

            return false
        }

        return true
    }

    private fun performTileSourceCopy(overwrite: Boolean) {
        viewModelScope.launch {
            val destinationFolder = getApplication<Application>().offlineTilesDir()

            if (checkFilenameExists(
                    uiState.value.sourceName,
                    destinationFolder
                ) && !overwrite
            ) {
                uiEventHandler(
                    OfflineTileSourceEvent.OnSourceNameError(
                        TileNameError.Exists
                    )
                )
            } else {
                uiEventHandler(
                    OfflineTileSourceEvent.OnLoadingValueChange(
                        true
                    )
                )

                uiState.value.fileUri?.let { uriString ->
                    copyFileToFolder(
                        contentResolver = getApplication<Application>().contentResolver,
                        fileUri = Uri.parse(uriString),
                        destinationFolder = destinationFolder,
                        fileName = uiState.value.sourceName
                    ).let {
                        uiEventHandler(
                            OfflineTileSourceEvent.OnLoadingValueChange(
                                true
                            )
                        )

                        if (it != null) {
                            val format = MbTilesLoader.getFormat(it)
                            val minMaxZoom = MbTilesLoader.getMinMaxZoom(it)

                            when (format) {

                                MbTileType.Vector -> {

                                    // Vector not supported right now
                                    deleteFileFromFolder(
                                        destinationFolder = destinationFolder,
                                        uiState.value.sourceName
                                    )
                                    uiEventHandler(OfflineTileSourceEvent.OnVectorUnsupported)
                                    return@launch

                                    /*saveOfflineVectorTileProvider(
                                        CustomTileProvider(
                                            CustomTileProviderType.Vector.Offline(
                                                name = uiState.value.sourceName,
                                                minZoom = minMaxZoom.first,
                                                maxZoom = minMaxZoom.second,
                                                filePath = it.absolutePath
                                            )
                                        )
                                    )*/
                                }

                                MbTileType.Raster -> {
                                    saveOfflineRasterTileProvider(
                                        CustomTileProvider(
                                            CustomTileProviderType.Raster.Offline(
                                                name = uiState.value.sourceName,
                                                minZoom = minMaxZoom.first,
                                                maxZoom = minMaxZoom.second,
                                                tileSize = uiState.value.tileSize,
                                                filePath = it.absolutePath
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        sendEffect(OfflineTileSourceEffect.NavigateBack)
                    }
                } ?: run {
                    uiEventHandler(
                        OfflineTileSourceEvent.OnLoadingValueChange(
                            false
                        )
                    )
                    sendEffect(OfflineTileSourceEffect.ShowMessage(R.string.incorrect_file_type_selected_message))
                }
            }
        }
    }

    private fun sendEffect(effect: OfflineTileSourceEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun saveOfflineRasterTileProvider(tileProvider: CustomTileProvider) {
        viewModelScope.launch {
            customOfflineRasterTileSourceRepository.saveOfflineRasterTileSourceProvider(tileProvider)
        }
    }

    fun saveOfflineVectorTileProvider(tileProvider: CustomTileProvider) {
        viewModelScope.launch {
            customOfflineVectorTileSourceRepository.saveOfflineVectorTileSourceProvider(tileProvider)
        }
    }
}