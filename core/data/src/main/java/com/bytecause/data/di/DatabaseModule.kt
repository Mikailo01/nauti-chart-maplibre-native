package com.bytecause.data.di

import android.content.Context
import androidx.room.Room
import com.bytecause.data.local.room.AppDatabase
import com.bytecause.data.local.room.callbacks.InitialPoiCategoryCallback
import com.bytecause.data.local.room.tables.AnchorageMovementTrackEntity
import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.HarboursEntity
import com.bytecause.data.local.room.tables.PoiCacheEntity
import com.bytecause.data.local.room.tables.RadiusPoiCacheEntity
import com.bytecause.data.local.room.tables.RadiusPoiMetadataDatasetEntity
import com.bytecause.data.local.room.tables.RouteRecordEntity
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
        .createFromAsset("database/continents_countries_anchorages.db")
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
    @Singleton
    fun provideOsmDatasetDao(db: AppDatabase) = db.osmRegionMetadataDatasetDao()

    @Provides
    @Singleton
    fun provideAnchoragesDao(db: AppDatabase) = db.anchoragesDao()

    @Provides
    @Singleton
    fun provideHarboursMetadataDatasetDao(db: AppDatabase) = db.harboursMetadataDatasetDao()

    @Provides
    @Singleton
    fun provideVesselsMetadataDatasetDao(db: AppDatabase) = db.vesselsMetadataDatasetDao()

    @Provides
    @Singleton
    fun provideCountryDao(db: AppDatabase) = db.countryDao()

    @Provides
    @Singleton
    fun provideRegionDao(db: AppDatabase) = db.regionDao()

    @Provides
    @Singleton
    fun providePoiCacheDao(db: AppDatabase) = db.poiCacheDao()

    @Provides
    @Singleton
    fun provideRadiusPoiCacheDao(db: AppDatabase) = db.radiusPoiCacheDao()

    @Provides
    @Singleton
    fun provideRadiusPoiMetadataDatasetDao(db: AppDatabase) = db.radiusPoiCacheMetadataDatasetDao()

    @Provides
    @Singleton
    fun provideAnchorageMovementTrackDao(db: AppDatabase) = db.anchorageMovementTrackDao()

    @Provides
    @Singleton
    fun provideTrackRouteDao(db: AppDatabase) = db.trackRouteDao()

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
    fun providePoiCacheEntity() = PoiCacheEntity()

    @Provides
    fun provideRadiusPoiCacheEntity() = RadiusPoiCacheEntity()

    @Provides
    fun provideRadiusPoiMetadataDatasetEntity() = RadiusPoiMetadataDatasetEntity()

    @Provides
    fun provideContinentCountries() = ContinentCountriesRelation()

    @Provides
    fun provideCountryRegions() = CountryRegionsRelation()

    @Provides
    fun provideAnchorageMovementTrackEntity() = AnchorageMovementTrackEntity()

    @Provides
    fun provideRouteRecordEntity() = RouteRecordEntity()
}