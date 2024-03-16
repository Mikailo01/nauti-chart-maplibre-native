package com.bytecause.nautichart.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bytecause.nautichart.data.local.room.tables.Continent
import com.bytecause.nautichart.data.local.room.tables.Country
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.data.local.room.tables.HarboursEntity
import com.bytecause.nautichart.data.local.room.tables.PoiCacheEntity
import com.bytecause.nautichart.data.local.room.tables.Region
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity

@Database(
    entities = [
        HarboursEntity::class,
        VesselInfoEntity::class,
        CustomPoiCategoryEntity::class,
        CustomPoiEntity::class,
        PoiCacheEntity::class,
        Country::class,
        Continent::class,
        Region::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // The database exposes DAOs through an abstract "getter" method for each @Dao.
    abstract fun harboursDao(): HarboursDao

    abstract fun vesselInfoDao(): VesselInfoDao

    //abstract fun customCategoryPoiDao(): CustomPoiCategoryDao

    abstract fun customPoiDao(): CustomPoiDao

    abstract fun searchCacheDao(): PoiCacheDao

    abstract fun continentDao(): ContinentDao

    abstract fun countryDao(): CountryDao

    abstract fun regionDao(): RegionDao
}