package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.domain.tilesources.TileSourceTypes
import com.bytecause.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.map.ui.bottomsheet.LayerTypes
import com.bytecause.map.ui.bottomsheet.MapBottomSheetResources
import com.bytecause.map.ui.model.LayersChildItem
import com.bytecause.util.file.FileUtil.deleteFileFromFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapBottomSheetViewModel
@Inject
constructor(
    private val userPreferencesRepository: dagger.Lazy<UserPreferencesRepository>,
    private val customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
    private val customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
    private val customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository,
    customTileSourcesUseCase: CustomTileSourcesUseCase,
) : ViewModel() {
    private val _contentMapStateFlow: MutableStateFlow<Map<LayerTypes, List<LayersChildItem>>?> =
        MutableStateFlow(null)

    init {
        getCustomTileSources()
    }

    val contentMapStateFlow: StateFlow<Map<LayerTypes, List<LayersChildItem>>?> =
        _contentMapStateFlow.asStateFlow()

    fun cacheSelectedTileSource(tileSourceName: String) {
        viewModelScope.launch {
            userPreferencesRepository.get().cacheSelectedTileSource(tileSourceName)
        }
    }

    val customTileSources: Flow<Map<TileSourceTypes, List<CustomTileProvider>>> =
        customTileSourcesUseCase()

    fun deleteCustomProvider(
        parentPosition: Int,
        childPosition: Int
    ): Flow<Pair<LayerTypes, String?>?> = flow {
        val deletedItemName: Pair<LayerTypes, String?>? =
            when (contentMapStateFlow.value!!.toList()[parentPosition].second[childPosition].layerType) {
                LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE -> {
                    LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE to customOnlineRasterTileSourceRepository.deleteOnlineRasterTileSourceProvider(
                        childPosition
                    )
                        .firstOrNull()
                }

                LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE -> {
                    LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE to customOfflineRasterTileSourceRepository.deleteOfflineRasterTileSourceProvider(
                        childPosition
                    )
                        .firstOrNull()
                }

                LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE -> {
                    LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE to customOfflineVectorTileSourceRepository.deleteOfflineVectorTileSourceProvider(
                        childPosition
                    )
                        .firstOrNull()
                }

                else -> null
            }

        // Update the content map state flow
        _contentMapStateFlow.value = _contentMapStateFlow.value!!.toMutableMap().apply {
            val mapKey = this.keys.elementAt(parentPosition)

            val updatedList = this[mapKey]?.toMutableList()?.apply {
                removeAt(childPosition)
            }

            if (updatedList.isNullOrEmpty()) {
                this.remove(mapKey)
            } else this[mapKey] = updatedList
        }

        // Emit the name of the deleted item
        emit(deletedItemName)
    }

    fun deleteOfflineTiles(destinationFolder: String, fileName: String) {
        viewModelScope.launch {
            deleteFileFromFolder(destinationFolder, fileName)
        }
    }

    private fun getCustomTileSources() {
        viewModelScope.launch {
            customTileSources.firstOrNull()?.let { customTileProviders ->
                if (customTileProviders.isEmpty()) return@let

                val sourcesMap: MutableMap<LayerTypes, List<LayersChildItem>> = mutableMapOf()

                customTileProviders.values.map {
                    it.map { provider ->
                        (provider.type as? CustomTileProviderType.Raster.Online)?.run {
                            LayersChildItem(
                                layerType = LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE,
                                resource = MapBottomSheetResources.Custom(
                                    name = name,
                                    imageUrl = imageUrl
                                ),
                            )
                        } ?: (provider.type as? CustomTileProviderType.Raster.Offline)?.run {
                            LayersChildItem(
                                layerType = LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE,
                                resource = MapBottomSheetResources.Custom(name),
                            )
                        } ?: (provider.type as CustomTileProviderType.Vector.Offline).run {
                            LayersChildItem(
                                layerType = LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE,
                                resource = MapBottomSheetResources.Custom(name),
                            )
                        }
                    }.let { layersList ->
                        val onlineRasterTileSources =
                            layersList.filter { tileSource -> tileSource.layerType == LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE }
                        val offlineRasterTileSources =
                            layersList.filter { tileSource -> tileSource.layerType == LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE }
                        val vectorTileSources =
                            layersList.filter { tileSource -> tileSource.layerType == LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE }

                        onlineRasterTileSources.takeIf { tileSources -> tileSources.isNotEmpty() }
                            ?.let { rasterSources ->
                                sourcesMap.put(
                                    LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE,
                                    rasterSources
                                )
                            }

                        offlineRasterTileSources.takeIf { tileSources -> tileSources.isNotEmpty() }
                            ?.let { rasterSources ->
                                sourcesMap.put(
                                    LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE,
                                    rasterSources
                                )
                            }

                        vectorTileSources.takeIf { tileSources -> tileSources.isNotEmpty() }
                            ?.let { vectorSources ->
                                sourcesMap.put(
                                    LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE,
                                    vectorSources
                                )
                            }
                    }
                }

                _contentMapStateFlow.emit(sourcesMap)
            }
        }
    }
}
