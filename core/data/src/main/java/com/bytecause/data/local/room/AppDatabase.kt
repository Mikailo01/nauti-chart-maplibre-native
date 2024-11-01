package com.bytecause.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bytecause.data.local.room.dao.AnchorageMovementTrackDao
import com.bytecause.data.local.room.dao.AnchoragesDao
import com.bytecause.data.local.room.dao.ContinentDao
import com.bytecause.data.local.room.dao.CountryDao
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.dao.HarboursDao
import com.bytecause.data.local.room.dao.HarboursMetadataDatasetDao
import com.bytecause.data.local.room.dao.OsmRegionMetadataDatasetDao
import com.bytecause.data.local.room.dao.PoiCacheDao
import com.bytecause.data.local.room.dao.RadiusPoiCacheDao
import com.bytecause.data.local.room.dao.RadiusPoiMetadataDatasetDao
import com.bytecause.data.local.room.dao.RegionDao
import com.bytecause.data.local.room.dao.TrackRouteDao
import com.bytecause.data.local.room.dao.VesselInfoDao
import com.bytecause.data.local.room.dao.VesselsMetadataDatasetDao
import com.bytecause.data.local.room.tables.AnchorageMovementTrackEntity
import com.bytecause.data.local.room.tables.AnchoragesEntity
import com.bytecause.data.local.room.tables.ContinentEntity
import com.bytecause.data.local.room.tables.CountryEntity
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.local.room.tables.HarboursEntity
import com.bytecause.data.local.room.tables.HarboursMetadataDatasetEntity
import com.bytecause.data.local.room.tables.OsmRegionMetadataDatasetEntity
import com.bytecause.data.local.room.tables.PoiCacheEntity
import com.bytecause.data.local.room.tables.RadiusPoiCacheEntity
import com.bytecause.data.local.room.tables.RadiusPoiMetadataDatasetEntity
import com.bytecause.data.local.room.tables.RegionEntity
import com.bytecause.data.local.room.tables.RouteRecordEntity
import com.bytecause.data.local.room.tables.VesselInfoEntity
import com.bytecause.data.local.room.tables.VesselsMetadataDatasetEntity

@Database(
    entities = [
        HarboursEntity::class,
        VesselInfoEntity::class,
        CustomPoiCategoryEntity::class,
        CustomPoiEntity::class,
        OsmRegionMetadataDatasetEntity::class,
        HarboursMetadataDatasetEntity::class,
        VesselsMetadataDatasetEntity::class,
        RadiusPoiMetadataDatasetEntity::class,
        PoiCacheEntity::class,
        RadiusPoiCacheEntity::class,
        CountryEntity::class,
        ContinentEntity::class,
        RegionEntity::class,
        AnchoragesEntity::class,
        AnchorageMovementTrackEntity::class,
        RouteRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // The database exposes DAOs through an abstract "getter" method for each @Dao.
    abstract fun harboursDao(): HarboursDao

    abstract fun vesselInfoDao(): VesselInfoDao

    abstract fun customPoiDao(): CustomPoiDao

    abstract fun poiCacheDao(): PoiCacheDao

    abstract fun radiusPoiCacheDao(): RadiusPoiCacheDao

    abstract fun continentDao(): ContinentDao

    abstract fun countryDao(): CountryDao

    abstract fun regionDao(): RegionDao

    abstract fun osmRegionMetadataDatasetDao(): OsmRegionMetadataDatasetDao

    abstract fun harboursMetadataDatasetDao(): HarboursMetadataDatasetDao

    abstract fun vesselsMetadataDatasetDao(): VesselsMetadataDatasetDao

    abstract fun radiusPoiCacheMetadataDatasetDao(): RadiusPoiMetadataDatasetDao

    abstract fun anchoragesDao(): AnchoragesDao

    abstract fun anchorageMovementTrackDao(): AnchorageMovementTrackDao

    abstract fun trackRouteDao(): TrackRouteDao
}