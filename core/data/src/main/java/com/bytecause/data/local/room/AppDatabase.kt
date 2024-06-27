package com.bytecause.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bytecause.data.local.room.dao.ContinentDao
import com.bytecause.data.local.room.dao.CountryDao
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.dao.HarboursDao
import com.bytecause.data.local.room.dao.PoiCacheDao
import com.bytecause.data.local.room.dao.RegionDao
import com.bytecause.data.local.room.dao.VesselInfoDao
import com.bytecause.data.local.room.tables.ContinentEntity
import com.bytecause.data.local.room.tables.CountryEntity
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.HarboursEntity
import com.bytecause.data.local.room.tables.PoiCacheEntity
import com.bytecause.data.local.room.tables.RegionEntity
import com.bytecause.data.local.room.tables.VesselInfoEntity

@Database(
    entities = [
        HarboursEntity::class,
        VesselInfoEntity::class,
        CustomPoiCategoryEntity::class,
        CustomPoiEntity::class,
        PoiCacheEntity::class,
        CountryEntity::class,
        ContinentEntity::class,
        RegionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // The database exposes DAOs through an abstract "getter" method for each @Dao.
    abstract fun harboursDao(): HarboursDao

    abstract fun vesselInfoDao(): VesselInfoDao

    abstract fun customPoiDao(): CustomPoiDao

    abstract fun searchCacheDao(): PoiCacheDao

    abstract fun continentDao(): ContinentDao

    abstract fun countryDao(): CountryDao

    abstract fun regionDao(): RegionDao
}