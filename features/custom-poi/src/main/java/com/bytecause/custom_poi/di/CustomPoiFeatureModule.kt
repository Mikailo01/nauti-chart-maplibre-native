package com.bytecause.custom_poi.di

import android.content.Context
import com.bytecause.custom_poi.data.repository.RecentlyUsedIconsRepositoryImpl
import com.bytecause.custom_poi.data.repository.abstraction.RecentlyUsedIconsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CustomPoiFeatureModule {

    @Provides
    @Singleton
    fun providesRecentlyUsedIconsRepository(@ApplicationContext context: Context): RecentlyUsedIconsRepository =
        RecentlyUsedIconsRepositoryImpl(context)
}