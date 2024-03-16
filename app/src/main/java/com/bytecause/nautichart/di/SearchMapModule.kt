package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.nautichart.data.remote.RegionDataExtractRemoteDataSource
import com.bytecause.nautichart.data.repository.OverpassRepository
import com.bytecause.nautichart.data.repository.SearchMapRepository
import com.bytecause.nautichart.data.remote.retrofit.NominatimRestApiBuilder
import com.bytecause.nautichart.data.remote.retrofit.NominatimRestApiService
import com.bytecause.nautichart.data.remote.retrofit.OverpassRestApiBuilder
import com.bytecause.nautichart.data.remote.retrofit.OverpassRestApiService
import com.bytecause.nautichart.data.repository.ContinentDatabaseRepository
import com.bytecause.nautichart.data.repository.CountryDataExtractSizeRepository
import com.bytecause.nautichart.data.repository.RegionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchMapModule {

    @Singleton
    @Provides
    fun providesNominatimRestApiService(): NominatimRestApiService =
        NominatimRestApiBuilder().getSearchApiService()

    @Singleton
    @Provides
    fun providesSearchMapRepository(): SearchMapRepository = SearchMapRepository(
        providesNominatimRestApiService()
    )

    @Singleton
    @Provides
    fun providesOverpassRestApiService(): OverpassRestApiService =
        OverpassRestApiBuilder().overpassSearch()

    @Singleton
    @Provides
    fun providesOverpassRepository(): OverpassRepository = OverpassRepository(
        providesOverpassRestApiService()
    )

    @Singleton
    @Provides
    fun providesRegionRepository(@ApplicationContext context: Context): RegionRepository =
        RegionRepository(
            DatabaseModule.provideRegionDao(
                DatabaseModule.provideDatabase(context)
            )
        )

    @Singleton
    @Provides
    fun providesContinentRepository(@ApplicationContext context: Context): ContinentDatabaseRepository =
        ContinentDatabaseRepository(
            DatabaseModule.provideContinentDao(
                DatabaseModule.provideDatabase(context)
            ),
            DatabaseModule.provideCountryDao(DatabaseModule.provideDatabase(context))
        )

    @Singleton
    @Provides
    fun providesCountryDataExtractSizeRepository(): CountryDataExtractSizeRepository =
        CountryDataExtractSizeRepository(
            providesRegionDataExtractRemoteDataSource()
        )

    @Singleton
    @Provides
    fun providesRegionDataExtractRemoteDataSource(): RegionDataExtractRemoteDataSource =
        RegionDataExtractRemoteDataSource()
}