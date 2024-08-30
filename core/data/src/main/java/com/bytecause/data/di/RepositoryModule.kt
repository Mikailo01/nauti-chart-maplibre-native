package com.bytecause.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.dao.PoiCacheDao
import com.bytecause.data.remote.retrofit.OverpassRestApiService
import com.bytecause.data.repository.CustomOfflineRasterTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomOfflineVectorTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomOnlineRasterTileSourceRepositoryImpl
import com.bytecause.data.repository.CustomPoiRepositoryImpl
import com.bytecause.data.repository.OverpassRepositoryImpl
import com.bytecause.data.repository.PoiCacheRepositoryImpl
import com.bytecause.data.repository.UserPreferencesRepositoryImpl
import com.bytecause.data.repository.abstractions.CustomPoiRepository
import com.bytecause.data.repository.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.CustomOfflineRasterTileSourceRepository
import com.bytecause.domain.abstractions.CustomOfflineVectorTileSourceRepository
import com.bytecause.domain.abstractions.CustomOnlineRasterTileSourceRepository
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
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
    fun providesOverpassRepository(overpassRestApiService: OverpassRestApiService): OverpassRepository =
        OverpassRepositoryImpl(overpassRestApiService)

    @Provides
    @Singleton
    fun providesPoiCacheRepository(poiCacheDao: PoiCacheDao): PoiCacheRepository =
        PoiCacheRepositoryImpl(poiCacheDao)

    @Provides
    @Singleton
    fun providesUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository =
        UserPreferencesRepositoryImpl(
            context.userDataStore
        )
}