package com.bytecause.search.di

import com.bytecause.search.data.remote.retrofit.NominatimRestApiBuilder
import com.bytecause.search.data.remote.retrofit.NominatimRestApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Singleton
    @Provides
    fun providesNominatimRestApiService(): NominatimRestApiService =
        NominatimRestApiBuilder().getSearchApiService()
}