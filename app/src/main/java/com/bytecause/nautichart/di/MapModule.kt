package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.nautichart.data.remote.HarboursRemoteDataSource
import com.bytecause.nautichart.data.remote.VesselsPositionsRemoteDataSource
import com.bytecause.nautichart.data.repository.HarboursRepository
import com.bytecause.nautichart.data.repository.VesselsDatabaseRepository
import com.bytecause.nautichart.data.repository.VesselsPositionsRepository
import com.bytecause.nautichart.domain.usecase.VesselsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    @Singleton
    @Provides
    fun providesHarboursRepository(): HarboursRepository = HarboursRepository(
        providesHarboursRemoteDataSource()
    )

    @Singleton
    @Provides
    fun providesHarboursRemoteDataSource(): HarboursRemoteDataSource = HarboursRemoteDataSource()

    @Singleton
    @Provides
    fun providesVesselsPositionsRepository(): VesselsPositionsRepository =
        VesselsPositionsRepository(
            providesVesselsRemoteDataSource()
        )

    @Singleton
    @Provides
    fun providesVesselsUseCase(@ApplicationContext context: Context): VesselsUseCase =
        VesselsUseCase(
            VesselsDatabaseRepository(
                vesselInfoDao = DatabaseModule.provideVesselInfoDao(
                    DatabaseModule.provideDatabase(
                        context
                    )
                )
            ),
            providesVesselsPositionsRepository()
        )

    @Singleton
    @Provides
    fun providesVesselsRemoteDataSource(): VesselsPositionsRemoteDataSource =
        VesselsPositionsRemoteDataSource()
}