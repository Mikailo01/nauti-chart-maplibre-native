package com.bytecause.map.ui.mappers

import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.domain.model.RadiusPoiCacheModel
import com.bytecause.domain.model.RunningAnchorageAlarmModel
import com.bytecause.map.ui.model.HarboursUiModel
import com.bytecause.map.ui.model.PoiUiModel
import com.bytecause.map.ui.model.PoiUiModelWithTags
import com.bytecause.map.ui.model.RunningAnchorageAlarmUiModel

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

internal fun RunningAnchorageAlarmModel.asRunningAnchorageAlarmUiModel(): RunningAnchorageAlarmUiModel =
    RunningAnchorageAlarmUiModel(
        isRunning = isRunning,
        latitude = latitude,
        longitude = longitude,
        radius = radius
    )