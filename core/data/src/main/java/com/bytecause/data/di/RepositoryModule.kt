package com.bytecause.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytecause.data.local.room.dao.CountryDao
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.dao.HarboursMetadataDatasetDao
import com.bytecause.data.local.room.dao.OsmRegionMetadataDatasetDao
import com.bytecause.data.local.room.dao.PoiCacheDao
import com.bytecause.data.local.room.dao.RadiusPoiCacheDao
import com.bytecause.data.local.room.dao.RadiusPoiMetadataDatasetDao
import com.bytecause.data.local.room.dao.VesselsMetadataDatasetDao
import com.bytecause.data.remote.retrofit.OverpassRestApiService
import com.bytecause.data.repository.AnchorageAlarmRepositoryImpl
import com.bytecause.data.repository.CountryRepositoryImpl
import com.bytecause.data.repository.CustomOfflineRasterTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomOfflineVectorTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomOnlineRasterTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomPoiRepositoryImpl
import com.bytecause.data.repository.HarboursMetadataDatasetRepositoryImpl
import com.bytecause.data.repository.OsmRegionMetadataDatasetRepositoryImpl
import com.bytecause.data.repository.OverpassRepositoryImpl
import com.bytecause.data.repository.PoiCacheRepositoryImpl
import com.bytecause.data.repository.RadiusPoiCacheRepositoryImpl
import com.bytecause.data.repository.RadiusPoiMetadataDatasetRepositoryImpl
import com.bytecause.data.repository.UserPreferencesRepositoryImpl
import com.bytecause.data.repository.VesselsMetadataDatasetRepositoryImpl
import com.bytecause.data.repository.abstractions.AnchorageAlarmRepository
import com.bytecause.data.repository.abstractions.CountryRepository
import com.bytecause.data.repository.abstractions.CustomPoiRepository
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.abstractions.HarboursMetadataDatasetRepository
import com.bytecause.domain.abstractions.OsmRegionMetadataDatasetRepository
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.RadiusPoiCacheRepository
import com.bytecause.domain.abstractions.RadiusPoiMetadataDatasetRepository
import com.bytecause.domain.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.VesselsMetadataDatasetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesCustomOfflineRasterTileSourceRepository(@ApplicationContext context: Context): CustomOfflineRasterTileSourceRepository =
        CustomOfflineRasterTileSourceRepositoryImpl(context)

    @Provides
    @Singleton
    fun providesCustomOnlineRasterTileSourceRepository(@ApplicationContext context: Context): CustomOnlineRasterTileSourceRepository =
        CustomOnlineRasterTileSourceRepositoryImpl(context)

    @Provides
    @Singleton
    fun providesCustomOfflineVectorTileSourceRepository(@ApplicationContext context: Context): CustomOfflineVectorTileSourceRepository =
        CustomOfflineVectorTileSourceRepositoryImpl(context)

    @Provides
    @Singleton
    fun providesCustomPoiRepository(customPoiDao: CustomPoiDao): CustomPoiRepository =
        CustomPoiRepositoryImpl(customPoiDao)

    @Provides
    @Singleton
    fun providesOverpassRepository(
        overpassRestApiService: OverpassRestApiService,
    ): OverpassRepository = OverpassRepositoryImpl(overpassRestApiService)

    @Provides
    @Singleton
    fun providesPoiCacheRepository(
        poiCacheDao: PoiCacheDao
    ): PoiCacheRepository = PoiCacheRepositoryImpl(poiCacheDao)

    @Provides
    @Singleton
    fun providesRadiusPoiCacheRepository(radiusPoiCacheDao: RadiusPoiCacheDao): RadiusPoiCacheRepository =
        RadiusPoiCacheRepositoryImpl(radiusPoiCacheDao)

    @Provides
    @Singleton
    fun providesOsmRegionMetadataDatasetRepository(osmRegionMetadataDatasetDao: OsmRegionMetadataDatasetDao): OsmRegionMetadataDatasetRepository =
        OsmRegionMetadataDatasetRepositoryImpl(osmRegionMetadataDatasetDao)

    @Provides
    @Singleton
    fun providesRadiusPoiMetadataDatasetRepository(radiusPoiMetadataDatasetDao: RadiusPoiMetadataDatasetDao): RadiusPoiMetadataDatasetRepository =
        RadiusPoiMetadataDatasetRepositoryImpl(radiusPoiMetadataDatasetDao = radiusPoiMetadataDatasetDao)

    @Provides
    @Singleton
    fun providesHarboursMetadataDatasetRepository(harboursMetadataDatasetDao: HarboursMetadataDatasetDao): HarboursMetadataDatasetRepository =
        HarboursMetadataDatasetRepositoryImpl(harboursMetadataDatasetDao)

    @Provides
    @Singleton
    fun providesVesselsMetadataDatasetRepository(vesselsMetadataDatasetDao: VesselsMetadataDatasetDao): VesselsMetadataDatasetRepository =
        VesselsMetadataDatasetRepositoryImpl(vesselsMetadataDatasetDao)

    @Provides
    @Singleton
    fun providesUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository =
        UserPreferencesRepositoryImpl(
            context.userDataStore
        )

    @Provides
    @Singleton
    fun providesAnchorageAlarmRepository(@ApplicationContext context: Context): AnchorageAlarmRepository =
        AnchorageAlarmRepositoryImpl(context = context)

    @Provides
    @Singleton
    fun providesCountryRepository(countryDao: CountryDao): CountryRepository =
        CountryRepositoryImpl(countryDao)
}