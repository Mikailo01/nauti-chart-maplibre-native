package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.tilesources.TileSourceTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CustomTileSourcesUseCase(
    private val offlineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
    private val onlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
    private val offlineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository
) {
    operator fun invoke(): Flow<Map<TileSourceTypes, List<CustomTileProvider>>> = combine(
        offlineRasterTileSourceRepository.getOfflineRasterTileSourceProviders(),
        onlineRasterTileSourceRepository.getOnlineRasterTileSourceProviders(),
        offlineVectorTileSourceRepository.getOfflineVectorTileSourceProviders()
    ) { offlineRasterProvider, onlineRasterProvider, offlineVectorProvider ->

        val providersMap = mutableMapOf<TileSourceTypes, List<CustomTileProvider>>()

        providersMap[TileSourceTypes.RasterOffline] = offlineRasterProvider
        providersMap[TileSourceTypes.RasterOnline] = onlineRasterProvider
        providersMap[TileSourceTypes.VectorOffline] = offlineVectorProvider

        providersMap
    }
}