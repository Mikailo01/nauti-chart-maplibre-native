package com.bytecause.search.di

import android.content.Context
import com.bytecause.search.data.repository.SearchHistoryRepositoryImpl
import com.bytecause.search.data.repository.SearchMapRepositoryImpl
import com.bytecause.data.repository.abstractions.SearchHistoryRepository
import com.bytecause.search.data.repository.abstractions.SearchMapRepository
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
    fun providesSearchMapRepository(): SearchMapRepository = SearchMapRepositoryImpl(
        ServiceModule.providesNominatimRestApiService()
    )

    @Singleton
    @Provides
    fun providesSearchHistoryRepository(@ApplicationContext context: Context): SearchHistoryRepository =
        SearchHistoryRepositoryImpl(context)

}