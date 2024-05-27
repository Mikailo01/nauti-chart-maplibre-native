package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.MapBottomSheetResources


data class LayersChildItem(
    val layerType: LayerTypes,
    val resource: MapBottomSheetResources,
    val origin: TileSourceOrigin
)

sealed interface TileSourceOrigin {
    data object Default : TileSourceOrigin
    data object CustomOffline : TileSourceOrigin
    data object CustomOnline : TileSourceOrigin
}