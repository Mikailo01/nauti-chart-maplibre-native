package com.bytecause.nautichart.di

import android.content.Context
import com.bytecause.nautichart.data.local.SearchManager
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
        SearchManager(context)
}