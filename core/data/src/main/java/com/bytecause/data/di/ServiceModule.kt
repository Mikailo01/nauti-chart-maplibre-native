package com.bytecause.data.di

import com.bytecause.data.remote.retrofit.OverpassRestApiBuilder
import com.bytecause.data.remote.retrofit.OverpassRestApiService
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
    fun providesOverpassRestApiService(): OverpassRestApiService =
        OverpassRestApiBuilder().overpassSearch()
}