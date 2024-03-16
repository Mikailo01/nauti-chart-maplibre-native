package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.nautichart.data.repository.SearchHistoryDataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProtoDataStoreModule {

    @Singleton
    @Provides
    fun providesSearchHistoryDataStoreRepo(@ApplicationContext context: Context): SearchHistoryDataStoreRepository = SearchHistoryDataStoreRepository(context)
}