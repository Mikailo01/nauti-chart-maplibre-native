package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.repository.CustomOfflineTileSourceRepositoryImpl
import com.bytecause.nautichart.data.repository.CustomOnlineTileSourceRepositoryImpl
import com.bytecause.nautichart.data.repository.UserPreferencesRepositoryImpl
import com.bytecause.nautichart.domain.model.CustomTileProvider
import com.bytecause.nautichart.domain.model.CustomTileProviderType
import com.bytecause.nautichart.domain.model.LayersChildItem
import com.bytecause.nautichart.domain.model.TileSourceOrigin
import com.bytecause.nautichart.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.MapBottomSheetResources
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
class MapBottomSheetViewModel @Inject constructor(
    private val repository: dagger.Lazy<UserPreferencesRepositoryImpl>,
    private val customOfflineTileSourceRepository: CustomOfflineTileSourceRepositoryImpl,
    private val customOnlineTileSourceRepository: CustomOnlineTileSourceRepositoryImpl,
    private val customTileSourcesUseCase: CustomTileSourcesUseCase
) : ViewModel() {

    private val _contentMapStateFlow: MutableStateFlow<Map<LayerTypes, List<LayersChildItem>>> =
        MutableStateFlow(
            mapOf(
                LayerTypes.TILE_SOURCE to listOf(
                    LayersChildItem(
                        layerType = LayerTypes.TILE_SOURCE,
                        resource = MapBottomSheetResources.Default,
                        origin = TileSourceOrigin.Default
                    ),
                    LayersChildItem(
                        layerType = LayerTypes.TILE_SOURCE,
                        resource = MapBottomSheetResources.Satellite,
                        origin = TileSourceOrigin.Default
                    ),
                    LayersChildItem(
                        layerType = LayerTypes.TILE_SOURCE,
                        resource = MapBottomSheetResources.Topo,
                        origin = TileSourceOrigin.Default
                    )
                ),
                LayerTypes.ADDITIONAL_OVERLAY to listOf(
                    LayersChildItem(
                        layerType = LayerTypes.ADDITIONAL_OVERLAY,
                        resource = MapBottomSheetResources.Grid,
                        origin = TileSourceOrigin.Default
                    )
                ).also { getCustomTileSources() }
            )
        )

    val contentMapStateFlow: StateFlow<Map<LayerTypes, List<LayersChildItem>>> =
        _contentMapStateFlow.asStateFlow()

    fun cacheSelectedTileSource(tileSourceName: String) {
        viewModelScope.launch {
            repository.get().cacheSelectedTileSource(tileSourceName)
        }
    }

    val customTileSources: Flow<List<CustomTileProvider>> = customTileSourcesUseCase.invoke()

    fun deleteCustomProvider(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (contentMapStateFlow.value.toList()[0].second[position].origin) {
                TileSourceOrigin.CustomOnline -> {
                    customOnlineTileSourceRepository.deleteOnlineTileSourceProvider(position - 3)
                }

                TileSourceOrigin.CustomOffline -> {
                    customOfflineTileSourceRepository.deleteOfflineTileSourceProvider(position - 3)
                }

                else -> {
                    // Do nothing
                }
            }

            _contentMapStateFlow.value = _contentMapStateFlow.value.toMutableMap().apply {
                this[LayerTypes.TILE_SOURCE] =
                    this[LayerTypes.TILE_SOURCE]?.minus(
                        this[LayerTypes.TILE_SOURCE]?.get(
                            position
                        ) ?: return@launch
                    ) ?: return@launch
            }
        }
    }

    private fun getCustomTileSources() {
        viewModelScope.launch {
            customTileSources.firstOrNull()?.let { customTileProviders ->
                if (customTileProviders.isEmpty()) return@let

                customTileProviders.map {
                    when (it.type) {
                        is CustomTileProviderType.Offline -> {
                            LayersChildItem(
                                layerType = LayerTypes.TILE_SOURCE,
                                resource = MapBottomSheetResources.Custom(it.type.name),
                                origin = TileSourceOrigin.CustomOffline
                            )
                        }

                        is CustomTileProviderType.Online -> {
                            LayersChildItem(
                                layerType = LayerTypes.TILE_SOURCE,
                                resource = MapBottomSheetResources.Custom(it.type.name),
                                origin = TileSourceOrigin.CustomOnline
                            )
                        }
                    }
                }.let {
                    _contentMapStateFlow.value = _contentMapStateFlow.value.toMutableMap().apply {
                        this[LayerTypes.TILE_SOURCE] =
                            this[LayerTypes.TILE_SOURCE]?.plus(it) ?: emptyList()
                    }
                }
            }
        }
    }
}