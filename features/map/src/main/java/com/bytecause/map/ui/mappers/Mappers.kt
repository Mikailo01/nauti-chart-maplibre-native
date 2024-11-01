package com.bytecause.map.ui.mappers

import com.bytecause.data.local.room.tables.RouteRecordEntity
import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.RadiusPoiCacheModel
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.HarboursUiModel
import com.bytecause.map.ui.model.PoiUiModel
import com.bytecause.map.ui.model.PoiUiModelWithTags
import com.bytecause.map.ui.model.TrackedRouteItem
import com.bytecause.nautichart.AnchorageHistory

internal fun PoiCacheModel.asPoiUiModel(): PoiUiModel = PoiUiModel(
    id = placeId,
    name = tags["name"] ?: "",
    category = category,
    latitude = latitude,
    longitude = longitude
)

internal fun PoiCacheModel.asPoiUiModelWithTags(): PoiUiModelWithTags = PoiUiModelWithTags(
    category = category,
    latitude = latitude,
    longitude = longitude,
    tags = tags
)

internal fun RadiusPoiCacheModel.asPoiUiModelWithTags(): PoiUiModelWithTags = PoiUiModelWithTags(
    category = category,
    latitude = latitude,
    longitude = longitude,
    tags = tags
)

internal fun RadiusPoiCacheModel.asPoiUiModel(): PoiUiModel = PoiUiModel(
    id = placeId,
    name = tags["name"] ?: "",
    category = category,
    latitude = latitude,
    longitude = longitude
)

internal fun HarboursModel.asHarbourUiModel(): HarboursUiModel = HarboursUiModel(
    id = id,
    latitude = latitude,
    longitude = longitude
)

internal fun AnchorageHistoryUiModel.asAnchorageHistory(): AnchorageHistory =
    AnchorageHistory.newBuilder()
        .setId(id)
        .setLatitude(latitude)
        .setLongitude(longitude)
        .setRadius(radius)
        .setTimestamp(timestamp)
        .build()

internal fun AnchorageHistory.asAnchorageHistoryUiModel(): AnchorageHistoryUiModel =
    AnchorageHistoryUiModel(
        id = id,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        timestamp = timestamp
    )

internal fun RouteRecordEntity.asTrackedRouteItem(): TrackedRouteItem = TrackedRouteItem(
    id = id,
    name = name,
    description = description,
    distance = distance,
    duration = duration,
    dateCreated = dateCreated
)