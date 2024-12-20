package com.bytecause.data.mappers

import com.bytecause.data.local.room.tables.AnchorageMovementTrackEntity
import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import com.bytecause.data.local.room.tables.ContinentEntity
import com.bytecause.data.local.room.tables.CountryEntity
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
import com.bytecause.data.local.room.tables.relations.CountryRegionsRelation
import com.bytecause.data.model.AnchorageMovementTrackModel
import com.bytecause.domain.model.ContinentCountriesModel
import com.bytecause.domain.model.ContinentModel
import com.bytecause.domain.model.CountryModel
import com.bytecause.domain.model.CountryRegionsModel
import com.bytecause.domain.model.HarboursMetadataDatasetModel
import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.OsmRegionMetadataDatasetModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.RadiusPoiCacheModel
import com.bytecause.domain.model.RadiusPoiMetadataDatasetModel
import com.bytecause.domain.model.RegionModel
import com.bytecause.domain.model.RouteRecordModel
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import com.bytecause.domain.model.VesselsMetadataDatasetModel
import com.bytecause.util.mappers.mapList


fun VesselInfoEntity.asVesselInfoModel(): VesselInfoModel =
    VesselInfoModel(
        id = id,
        latitude = latitude,
        longitude = longitude,
        name = name,
        type = type,
        heading = heading,
        speed = speed,
        flag = flag,
        mmsi = mmsi,
        length = length,
        eta = eta,
        timeStamp = timeStamp
    )

fun VesselInfoEntity.asVesselModel(): VesselModel =
    VesselModel(
        id = id,
        latitude = latitude.toDouble(),
        longitude = longitude.toDouble(),
        type = type,
        heading = heading
    )

fun VesselInfoModel.asVesselInfoEntity(): VesselInfoEntity =
    VesselInfoEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        name = name,
        type = type,
        heading = heading,
        speed = speed,
        flag = flag,
        mmsi = mmsi,
        length = length,
        eta = eta,
        timeStamp = timeStamp
    )

fun HarboursEntity.asHarboursModel(): HarboursModel = HarboursModel(
    id = id,
    latitude = latitude,
    longitude = longitude,
    tags = tags
)

fun HarboursModel.asHarboursEntity(): HarboursEntity = HarboursEntity(
    id = id,
    latitude = latitude,
    longitude = longitude,
    tags = tags
)

fun PoiCacheModel.asRegionPoiCacheEntity(): PoiCacheEntity =
    PoiCacheEntity(
        placeId = placeId,
        category = category,
        latitude = latitude,
        longitude = longitude,
        tags = tags,
        datasetId = datasetId
    )

fun PoiCacheEntity.asPoiCacheModel(): PoiCacheModel = PoiCacheModel(
    placeId = placeId,
    category = category,
    latitude = latitude,
    longitude = longitude,
    tags = tags,
    datasetId = datasetId
)

fun RadiusPoiCacheModel.asRadiusPoiCacheEntity(): RadiusPoiCacheEntity = RadiusPoiCacheEntity(
    placeId = placeId,
    category = category,
    latitude = latitude,
    longitude = longitude,
    tags = tags,
    datasetCategoryName = datasetCategoryName
)

fun RadiusPoiCacheEntity.asRadiusPoiCacheModel(): RadiusPoiCacheModel = RadiusPoiCacheModel(
    placeId = placeId,
    category = category,
    latitude = latitude,
    longitude = longitude,
    tags = tags,
    datasetCategoryName = datasetCategoryName
)

fun RegionEntity.asRegionModel(): RegionModel =
    RegionModel(id = id, names = names, countryId = countryId, isDownloaded = isDownloaded)


fun RegionModel.asRegionEntity(): RegionEntity =
    RegionEntity(id = id, names = names, countryId = countryId, isDownloaded = isDownloaded)

fun CountryEntity.asCountryModel(): CountryModel = CountryModel(
    id = id,
    name = name,
    iso2 = iso2,
    iso3 = iso3,
    continentId = continentId
)

fun ContinentEntity.asContinentModel(): ContinentModel = ContinentModel(id = id, name = name)

fun ContinentCountriesRelation.asContinentCountriesModel(): ContinentCountriesModel =
    ContinentCountriesModel(
        continentModel = continentEntity.asContinentModel(),
        countries = mapList(countries) { it.asCountryModel() }
    )

fun CountryRegionsRelation.asCountryRegionsModel() = CountryRegionsModel(
    countryModel = countryEntity.asCountryModel(),
    regionModels = mapList(regionEntities) { it.asRegionModel() }
)

fun OsmRegionMetadataDatasetModel.asOsmRegionMetadataDatasetEntity() =
    OsmRegionMetadataDatasetEntity(
        id = id,
        timestamp = timestamp
    )

fun OsmRegionMetadataDatasetEntity.asOsmRegionMetadataDatasetModel() =
    OsmRegionMetadataDatasetModel(
        id = id,
        timestamp = timestamp
    )

fun HarboursMetadataDatasetModel.asHarboursMetadataDatasetEntity(): HarboursMetadataDatasetEntity =
    HarboursMetadataDatasetEntity(
        timestamp = timestamp
    )

fun HarboursMetadataDatasetEntity.asHarboursMetadataDatasetModel(): HarboursMetadataDatasetModel =
    HarboursMetadataDatasetModel(
        timestamp = timestamp
    )

fun VesselsMetadataDatasetModel.asVesselsMetadataDatasetEntity(): VesselsMetadataDatasetEntity =
    VesselsMetadataDatasetEntity(
        timestamp = timestamp
    )

fun VesselsMetadataDatasetEntity.asVesselsMetadataDatasetModel(): VesselsMetadataDatasetModel =
    VesselsMetadataDatasetModel(
        timestamp = timestamp
    )

fun RadiusPoiMetadataDatasetEntity.asRadiusPoiMetadataDatasetModel(): RadiusPoiMetadataDatasetModel =
    RadiusPoiMetadataDatasetModel(
        category = category,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        timestamp = timestamp
    )

fun RadiusPoiMetadataDatasetModel.asRadiusPoiMetadataDatasetEntity(): RadiusPoiMetadataDatasetEntity =
    RadiusPoiMetadataDatasetEntity(
        category = category,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        timestamp = timestamp
    )

fun AnchorageMovementTrackModel.asAnchorageMovementTrackEntity(): AnchorageMovementTrackEntity =
    AnchorageMovementTrackEntity(
        latitude = latitude,
        longitude = longitude
    )

fun AnchorageMovementTrackEntity.asAnchorageMovementTrackModel(): AnchorageMovementTrackModel =
    AnchorageMovementTrackModel(
        latitude = latitude,
        longitude = longitude
    )

fun RouteRecordEntity.asRouteRecordModel(): RouteRecordModel =
    RouteRecordModel(
        id = id,
        name = name,
        description = description,
        distance = distance,
        startTime = startTime,
        dateCreated = dateCreated,
        points = points,
        speed = speed
    )

fun RouteRecordModel.asRouteRecordEntity(): RouteRecordEntity =
    RouteRecordEntity(
        id = id,
        name = name,
        description = description,
        distance = distance,
        startTime = startTime,
        dateCreated = dateCreated,
        points = points,
        speed = speed
    )