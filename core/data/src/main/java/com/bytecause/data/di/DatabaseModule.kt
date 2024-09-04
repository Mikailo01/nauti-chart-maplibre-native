package com.bytecause.data.di

import android.content.Context
import androidx.room.Room
import com.bytecause.data.local.room.AppDatabase
import com.bytecause.data.local.room.callbacks.InitialPoiCategoryCallback
import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.HarboursEntity
import com.bytecause.data.local.room.tables.RegionPoiCacheEntity
import com.bytecause.data.local.room.tables.VesselInfoEntity
import com.bytecause.data.local.room.tables.relations.CountryRegionsRelation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "app_database"
    )
        .createFromAsset("database/continents_with_countries.db")
        .addCallback(InitialPoiCategoryCallback(context))
        .build()

    /*private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

        }
    }*/

    // Provide DAOs.
    @Provides
    @Singleton
    fun provideHarboursDao(db: AppDatabase) = db.harboursDao()

    @Provides
    @Singleton
    fun provideVesselInfoDao(db: AppDatabase) = db.vesselInfoDao()

    @Provides
    @Singleton
    fun provideCustomPoiDao(db: AppDatabase) = db.customPoiDao()

    @Provides
    @Singleton
    fun provideContinentDao(db: AppDatabase) = db.continentDao()

    @Provides
    fun provideOsmDatasetDao(db: AppDatabase) = db.osmRegionMetadataDatasetDao()

    @Provides
    @Singleton
    fun provideCountryDao(db: AppDatabase) = db.countryDao()

    @Provides
    @Singleton
    fun provideRegionDao(db: AppDatabase) = db.regionDao()

    @Provides
    @Singleton
    fun provideRegionPoiCacheDao(db: AppDatabase) = db.regionPoiCacheDao()

    @Provides
    @Singleton
    fun provideRadiusPoiCacheDao(db: AppDatabase) = db.radiusPoiCacheDao()

    // Provide entities.
    @Provides
    fun provideHarboursEntity() = HarboursEntity()

    @Provides
    fun provideVesselInfoEntity() = VesselInfoEntity()

    @Provides
    fun provideCustomPoiEntity() = CustomPoiEntity()

    @Provides
    fun provideCustomPoiCategoryEntity() = CustomPoiCategoryEntity()

    @Provides
    fun providePoiCacheEntity() = RegionPoiCacheEntity()

    @Provides
    fun provideContinentCountries() = ContinentCountriesRelation()

    @Provides
    fun provideCountryRegions() = CountryRegionsRelation()
}