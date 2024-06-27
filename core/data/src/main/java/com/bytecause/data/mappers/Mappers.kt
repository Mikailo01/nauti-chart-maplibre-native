package com.bytecause.data.mappers

import com.bytecause.data.local.room.tables.ContinentCountriesRelation
import com.bytecause.data.local.room.tables.ContinentEntity
import com.bytecause.data.local.room.tables.CountryEntity
import com.bytecause.data.local.room.tables.PoiCacheEntity
import com.bytecause.data.local.room.tables.RegionEntity
import com.bytecause.data.local.room.tables.VesselInfoEntity
import com.bytecause.data.local.room.tables.relations.CountryRegionsRelation
import com.bytecause.domain.model.ContinentCountriesModel
import com.bytecause.domain.model.ContinentModel
import com.bytecause.domain.model.CountryModel
import com.bytecause.domain.model.CountryRegionsModel
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.RegionModel
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import com.bytecause.nautichart.CustomOfflineRasterTileSource
import com.bytecause.nautichart.CustomOnlineRasterTileSource


fun List<VesselInfoEntity>.asVesselModelList(): List<VesselModel> = this.map {
    VesselModel(
        id = it.id.toString(),
        latitude = it.latitude.toDouble(),
        longitude = it.longitude.toDouble(),
        type = it.type,
        heading = it.heading
    )
}

fun List<VesselInfoModel>.asVesselInfoEntityList(): List<VesselInfoEntity> = this.map {
    VesselInfoEntity(
        id = it.id,
        latitude = it.latitude,
        longitude = it.longitude,
        name = it.name,
        type = it.type,
        heading = it.heading,
        speed = it.speed,
        flag = it.flag,
        mmsi = it.mmsi,
        length = it.length,
        eta = it.eta,
        timeStamp = it.timeStamp
    )
}

fun CustomOnlineRasterTileSource.asRasterTileProvider() = CustomTileProvider(
    type = CustomTileProviderType.Raster.Online(
        name = name,
        url = url,
        tileFileFormat = tileFileFormat,
        minZoom = minZoom,
        maxZoom = maxZoom,
        tileSize = tileSize
    )
)

fun CustomOfflineRasterTileSource.asRasterTileProvider() = CustomTileProvider(
    type = CustomTileProviderType.Raster.Offline(
        name = name,
        minZoom = minZoom,
        maxZoom = maxZoom,
        tileSize = tileSize,
        filePath = filePath
    )
)

fun VesselInfoEntity.asVesselInfoModel(): VesselInfoModel = VesselInfoModel(
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

fun PoiCacheModel.asPoiCacheEntity(drawableResourceName: String? = null): PoiCacheEntity =
    PoiCacheEntity(
        placeId = placeId,
        category = category,
        drawableResourceName = drawableResourceName.takeIf { it != null }
            ?: this.drawableResourceName,
        latitude = latitude,
        longitude = longitude,
        tags = tags
    )

fun PoiCacheEntity.asPoiCacheModel(): PoiCacheModel = PoiCacheModel(
    placeId = placeId,
    category = category,
    drawableResourceName = drawableResourceName,
    latitude = latitude,
    longitude = longitude,
    tags = tags
)


fun List<RegionEntity>.asRegionModelList(): List<RegionModel> = this.map {
    RegionModel(
        id = it.id,
        names = it.names,
        countryId = it.countryId
    )
}

fun List<RegionModel>.asRegionEntityList(): List<RegionEntity> = this.map {
    RegionEntity(
        id = it.id,
        names = it.names,
        countryId = it.countryId
    )
}

fun CountryEntity.asCountryModel(): CountryModel = CountryModel(
    id = id,
    name = name,
    iso2 = iso2,
    iso3 = iso3,
    continentId = continentId
)

fun List<CountryEntity>.asCountryModelList(): List<CountryModel> = this.map {
    CountryModel(
        id = it.id,
        name = it.name,
        iso2 = it.iso2,
        iso3 = it.iso3,
        continentId = it.continentId
    )
}

fun ContinentEntity.asContinentModel(): ContinentModel = ContinentModel(id = id, name = name)

fun List<ContinentEntity>.asContinentModelList(): List<ContinentModel> = this.map {
    ContinentModel(
        id = it.id,
        name = it.name
    )
}

fun ContinentCountriesRelation.asContinentCountriesModel(): ContinentCountriesModel =
    ContinentCountriesModel(
        continentModel = continentEntity.asContinentModel(),
        countries = countries.asCountryModelList()
    )

fun CountryRegionsRelation.asCountryRegionsModel() = CountryRegionsModel(
    countryModel = countryEntity.asCountryModel(),
    regionModels = regionEntities.asRegionModelList()
)