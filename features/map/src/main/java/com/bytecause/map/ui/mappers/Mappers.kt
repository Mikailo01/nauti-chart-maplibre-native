package com.bytecause.map.ui.mappers

import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.PoiCacheModel
import com.bytecause.map.ui.model.HarboursUiModel
import com.bytecause.map.ui.model.PoiUiModel

internal fun PoiCacheModel.asPoiUiModel(): PoiUiModel = PoiUiModel(
    id = placeId,
    name = tags["name"] ?: "",
    category = category,
    drawableResourceName = drawableResourceName,
    latitude = latitude,
    longitude = longitude
)

internal fun HarboursModel.asHarbourUiModel(): HarboursUiModel = HarboursUiModel(
    id = id,
    latitude = latitude,
    longitude = longitude
)