package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.data.repository.CustomOfflineVectorTileSourceRepositoryImpl
import com.bytecause.data.repository.OverpassRepositoryImpl
import com.bytecause.data.repository.PoiCacheRepositoryImpl
import com.bytecause.data.repository.RegionRepositoryImpl
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.usecase.CustomTileSourcesUseCase
import com.bytecause.domain.usecase.FetchVesselsUseCase
import com.bytecause.domain.usecase.GetPoiResultByRadiusUseCase
import com.bytecause.domain.usecase.GetPoiResultByRegionUseCase
import com.bytecause.domain.usecase.GetRegionsUseCase
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
        regionRepository: RegionRepositoryImpl,
        overpassRepositoryImpl: OverpassRepositoryImpl
    ): GetRegionsUseCase = GetRegionsUseCase(regionRepository, overpassRepositoryImpl)

    @Provides
    fun providesGetPoiResultByRegionUseCase(
        overpassRepositoryImpl: OverpassRepositoryImpl,
        poiCacheRepositoryImpl: PoiCacheRepositoryImpl
    ): GetPoiResultByRegionUseCase =
        GetPoiResultByRegionUseCase(overpassRepositoryImpl, poiCacheRepositoryImpl)

    @Provides
    fun providesGetPoiResultByRadiusUseCase(
        poiCacheRepositoryImpl: PoiCacheRepositoryImpl,
        overpassRepositoryImpl: OverpassRepositoryImpl
    ): GetPoiResultByRadiusUseCase =
        GetPoiResultByRadiusUseCase(poiCacheRepositoryImpl, overpassRepositoryImpl)

    @Provides
    fun providesFetchVesselsUseCase(@ApplicationContext context: Context): FetchVesselsUseCase =
        FetchVesselsUseCase(
            providesVesselsDatabaseRepository(context),
            providesVesselsPositionsRepository()
        )

    @Provides
    fun providesCustomTileSourcesUseCase(
        customOfflineRasterTileSourceRepository: CustomOfflineRasterTileSourceRepository,
        customOnlineRasterTileSourceRepository: CustomOnlineRasterTileSourceRepository,
        customOfflineVectorTileSourceRepository: CustomOfflineVectorTileSourceRepository
    ): CustomTileSourcesUseCase =
        CustomTileSourcesUseCase(
            customOfflineRasterTileSourceRepository,
            customOnlineRasterTileSourceRepository,
            customOfflineVectorTileSourceRepository
        )
}