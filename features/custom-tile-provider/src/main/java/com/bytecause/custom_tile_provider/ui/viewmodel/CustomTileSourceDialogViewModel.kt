package com.bytecause.custom_tile_provider.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.custom_tile_provider.data.repository.abstractions.GetTileImageRepository
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomTileSourceDialogViewModel @Inject constructor(
    private val customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
    private val customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
    private val customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository,
    private val getTileImageRepository: GetTileImageRepository
) : ViewModel() {

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