package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.abstractions.HarboursMetadataDatasetRepository
import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.abstractions.RegionPoiCacheRepository
import com.bytecause.domain.abstractions.RegionRepository
import com.bytecause.domain.abstractions.VesselsMetadataDatasetRepository
import com.bytecause.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.domain.usecase.GetHarboursUseCase
import com.bytecause.domain.usecase.GetPoiResultByRadiusUseCase
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.domain.usecase.GetRegionsUseCase
import com.bytecause.domain.usecase.GetVesselsUseCase
import com.bytecause.domain.usecase.UpdateHarboursUseCase
import com.bytecause.domain.usecase.UpdateVesselsUseCase
import com.bytecause.map.di.RepositoryModule.providesVesselsDatabaseRepository
import com.bytecause.map.di.RepositoryModule.providesVesselsPositionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
/**
 * This module provides UseCase dependencies for platform-agnostic domain kotlin library.
 * */
object UseCaseModule {

    @Provides
    fun providesGetRegionsUseCase(
        regionRepository: RegionRepository,
        overpassRepository: OverpassRepository
    ): GetRegionsUseCase = GetRegionsUseCase(
        regionRepository = regionRepository,
        overpassRepository = overpassRepository
    )

    @Provides
    fun providesGetPoiResultByRegionUseCase(
        overpassRepository: OverpassRepository,
        poiCacheRepository: RegionPoiCacheRepository,
        osmRegionMetadataDatasetRepository: OsmRegionMetadataDatasetRepository
    ): GetPoiResultByRegionUseCase =
        GetPoiResultByRegionUseCase(
            overpassRepository = overpassRepository,
            regionPoiCacheRepository = poiCacheRepository,
            osmRegionMetadataDatasetRepository = osmRegionMetadataDatasetRepository
        )

    @Provides
    fun providesGetPoiResultByRadiusUseCase(
        radiusPoiCacheRepository: RadiusPoiCacheRepository,
        overpassRepository: OverpassRepository
    ): GetPoiResultByRadiusUseCase =
        GetPoiResultByRadiusUseCase(
            radiusPoiCacheRepository = radiusPoiCacheRepository,
            overpassRepository = overpassRepository
        )

    @Provides
    fun providesGetVesselsUseCase(
        @ApplicationContext context: Context,
        vesselsMetadataDatasetRepository: VesselsMetadataDatasetRepository
    ): GetVesselsUseCase =
        GetVesselsUseCase(
            vesselsDatabaseRepository = providesVesselsDatabaseRepository(context = context),
            updateVesselsUseCase = providesUpdateVesselsIfNecessaryUseCase(
                context = context,
                vesselsMetadataDatasetRepository = vesselsMetadataDatasetRepository
            )
        )

    @Provides
    fun providesUpdateVesselsIfNecessaryUseCase(
        @ApplicationContext context: Context,
        vesselsMetadataDatasetRepository: VesselsMetadataDatasetRepository
    ): UpdateVesselsUseCase =
        UpdateVesselsUseCase(
            vesselsDatabaseRepository = providesVesselsDatabaseRepository(context = context),
            vesselsPositionsRemoteRepository = providesVesselsPositionsRepository(),
            vesselsMetadataDatasetRepository = vesselsMetadataDatasetRepository
        )

    @Provides
    fun providesUpdateHarboursUseCase(
        harboursDatabaseRepository: HarboursDatabaseRepository,
        overpassRepository: OverpassRepository,
        harboursMetadataDatasetRepository: HarboursMetadataDatasetRepository
    ): UpdateHarboursUseCase =
        UpdateHarboursUseCase(
            harboursDatabaseRepository = harboursDatabaseRepository,
            overpassRepository = overpassRepository,
            harboursMetadataDatasetRepository = harboursMetadataDatasetRepository
        )

    @Provides
    fun providesGetHarboursUseCase(
        harboursDatabaseRepository: HarboursDatabaseRepository,
        overpassRepository: OverpassRepository,
        harboursMetadataDatasetRepository: HarboursMetadataDatasetRepository
    ): GetHarboursUseCase =
        GetHarboursUseCase(
            harboursDatabaseRepository,
            providesUpdateHarboursUseCase(
                harboursDatabaseRepository = harboursDatabaseRepository,
                overpassRepository = overpassRepository,
                harboursMetadataDatasetRepository = harboursMetadataDatasetRepository
            )
        )

    @Provides
    fun providesCustomTileSourcesUseCase(
        customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
        customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
        customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository
    ): CustomTileSourcesUseCase =
        CustomTileSourcesUseCase(
            offlineRasterTileSourceRepository = customOfflineRasterTileSourceRepository,
            onlineRasterTileSourceRepository = customOnlineRasterTileSourceRepository,
            offlineVectorTileSourceRepository = customOfflineVectorTileSourceRepository
        )
}