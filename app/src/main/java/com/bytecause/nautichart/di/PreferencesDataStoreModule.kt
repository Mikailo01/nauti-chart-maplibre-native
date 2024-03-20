package com.bytecause.nautichart.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytecause.nautichart.data.repository.UserPreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object PreferencesDataStoreModule {

    @Provides
    @Singleton
    fun providesUserDataStorePreferences(@ApplicationContext applicationContext: Context): DataStore<Preferences> =
        applicationContext.userDataStore

    @Provides
    @Singleton
    fun providesUserPreferencesRepository(@ApplicationContext applicationContext: Context): UserPreferencesRepositoryImpl = UserPreferencesRepositoryImpl(
        providesUserDataStorePreferences(applicationContext)
    )
}