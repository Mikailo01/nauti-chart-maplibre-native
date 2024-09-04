package com.bytecause.search.di

import android.content.Context
import com.bytecause.data.repository.abstractions.SearchManager
import com.bytecause.search.data.local.appsearch.SearchManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppSearchManagerModule {

    @Singleton
    @Provides
    fun providesSearchManager(@ApplicationContext context: Context): SearchManager =
        SearchManagerImpl(context)
}