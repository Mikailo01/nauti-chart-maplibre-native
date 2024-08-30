package com.bytecause.pois.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytecause.data.di.DatabaseModule
import com.bytecause.pois.data.remote.RegionDataExtractRemoteDataSource
import com.bytecause.pois.data.repository.ContinentRepositoryImpl
import com.bytecause.pois.data.repository.CountryDataExtractSizeRepositoryImpl
import com.bytecause.pois.data.repository.DownloadedRegionsRepositoryImpl
import com.bytecause.pois.data.repository.abstractions.ContinentRepository
import com.bytecause.pois.data.repository.abstractions.CountryDataExtractSizeRepository
import com.bytecause.data.repository.abstractions.DownloadedRegionsRepository
import com.bytecause.domain.abstractions.RegionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.downloadedRegionsDatastore: DataStore<Preferences> by preferencesDataStore(
    name = "downloaded_regions"
)

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun providesContinentRepository(@ApplicationContext context: Context): ContinentRepository =
        ContinentRepositoryImpl(
            DatabaseModule.provideContinentDao(
                DatabaseModule.provideDatabase(context)
            ),
            DatabaseModule.provideCountryDao(DatabaseModule.provideDatabase(context))
        )

    @Singleton
    @Provides
    fun providesCountryDataExtractSizeRepository(): CountryDataExtractSizeRepository =
        CountryDataExtractSizeRepositoryImpl(
            providesRegionDataExtractRemoteDataSource()
        )

    @Singleton
    @Provides
    fun providesDownloadedRegionsRepository(@ApplicationContext context: Context): DownloadedRegionsRepository =
        DownloadedRegionsRepositoryImpl(context.downloadedRegionsDatastore)

    @Singleton
    @Provides
    fun providesRegionDataExtractRemoteDataSource(): RegionDataExtractRemoteDataSource =
        RegionDataExtractRemoteDataSource()

    @Singleton
    @Provides
    fun providesRegionRepository(@ApplicationContext context: Context): RegionRepository =
        com.bytecause.pois.data.repository.RegionRepositoryImpl(
            DatabaseModule.provideRegionDao(
                DatabaseModule.provideDatabase(context)
            )
        )
}