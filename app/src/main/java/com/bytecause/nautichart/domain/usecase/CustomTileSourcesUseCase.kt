package com.bytecause.nautichart.domain.usecase

import com.bytecause.nautichart.CustomOfflineTileSourceList
import com.bytecause.nautichart.CustomOnlineTileSourceList
import com.bytecause.nautichart.data.repository.abstractions.CustomOfflineTileSourceRepository
import com.bytecause.nautichart.data.repository.abstractions.CustomOnlineTileSourceRepository
import com.bytecause.nautichart.domain.model.CustomTileProvider
import com.bytecause.nautichart.domain.model.CustomTileProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CustomTileSourcesUseCase(
    private val offlineTileSourceRepository: CustomOfflineTileSourceRepository,
    private val onlineTileSourceRepository: CustomOnlineTileSourceRepository
) {

    private fun getTileProviders(): Flow<List<CustomTileProvider>> = combine(
        offlineTileSourceRepository.getOfflineTileSourceProviders(),
        onlineTileSourceRepository.getOnlineTileSourceProviders()
    ) { offlineProvider, onlineProvider ->
        val providersList = mutableListOf<CustomTileProvider>()

        providersList.addAll(mapOfflineTileProvider(offlineProvider))
        providersList.addAll(mapOnlineTileProvider(onlineProvider))
        providersList
    }

    private fun mapOfflineTileProvider(tileProviders: CustomOfflineTileSourceList): List<CustomTileProvider> {
        return tileProviders.offlineTileSourceOrBuilderList.takeIf { it.isNotEmpty() }?.map {
            CustomTileProvider(
                type = CustomTileProviderType.Offline(
                    name = it.name,
                    minZoom = it.minZoom,
                    maxZoom = it.maxZoom,
                    tileSize = it.tileSize
                )
            )
        } ?: emptyList()
    }

    private fun mapOnlineTileProvider(tileProviders: CustomOnlineTileSourceList): List<CustomTileProvider> {
        return tileProviders.onlineTileSourceOrBuilderList.takeIf { it.isNotEmpty() }?.map {
            CustomTileProvider(
                type = CustomTileProviderType.Online(
                    name = it.name,
                    url = it.url,
                    tileFileFormat = it.tileFileFormat,
                    schema = it.schema,
                    minZoom = it.minZoom,
                    maxZoom = it.maxZoom,
                    tileSize = it.tileSize
                )
            )
        } ?: emptyList()
    }

    operator fun invoke(): Flow<List<CustomTileProvider>> = getTileProviders()
}