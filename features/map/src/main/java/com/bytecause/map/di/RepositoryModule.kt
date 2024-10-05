package com.bytecause.map.di

import android.content.Context
import com.bytecause.data.di.DatabaseModule
import com.bytecause.data.local.room.dao.AnchoragesDao
import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.abstractions.VesselsPositionsRemoteRepository
import com.bytecause.map.data.remote.VesselsPositionsRemoteDataSource
import com.bytecause.map.data.repository.AnchoragesRepositoryImpl
import com.bytecause.map.data.repository.HarboursDatabaseRepositoryImpl
import com.bytecause.map.data.repository.VesselsDatabaseRepositoryImpl
import com.bytecause.map.data.repository.VesselsRemoteRepositoryImpl
import com.bytecause.map.data.repository.abstraction.AnchoragesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesHarboursDatabaseRepository(@ApplicationContext context: Context): HarboursDatabaseRepository =
        HarboursDatabaseRepositoryImpl(
            DatabaseModule.provideHarboursDao(DatabaseModule.provideDatabase(context))
        )

    @Provides
    @Singleton
    fun providesVesselsDatabaseRepository(@ApplicationContext context: Context): VesselsDatabaseRepository =
        VesselsDatabaseRepositoryImpl(
            DatabaseModule.provideVesselInfoDao(
                DatabaseModule.provideDatabase(
                    context
                )
            )
        )

    @Provides
    @Singleton
    fun providesVesselsPositionsRepository(): VesselsPositionsRemoteRepository =
        VesselsRemoteRepositoryImpl(
            providesVesselsRemoteDataSource(),
        )

    @Provides
    @Singleton
    fun providesVesselsRemoteDataSource(): VesselsPositionsRemoteDataSource =
        VesselsPositionsRemoteDataSource()

    @Provides
    @Singleton
    fun providesAnchoragesRepository(anchoragesDao: AnchoragesDao): AnchoragesRepository =
        AnchoragesRepositoryImpl(anchoragesDao)
}