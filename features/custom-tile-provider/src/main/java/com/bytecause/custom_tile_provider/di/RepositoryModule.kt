package com.bytecause.custom_tile_provider.di

import com.bytecause.custom_tile_provider.data.remote.GetTileImageRemoteDataSource
import com.bytecause.custom_tile_provider.data.repository.GetTileImageRepositoryImpl
import com.bytecause.custom_tile_provider.data.repository.abstractions.GetTileImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesGetTileImageRepository(): GetTileImageRepository =
        GetTileImageRepositoryImpl(GetTileImageRemoteDataSource())
}