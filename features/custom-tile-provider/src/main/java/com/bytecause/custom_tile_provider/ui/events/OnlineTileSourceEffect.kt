package com.bytecause.custom_tile_provider.ui.events

sealed interface OnlineTileSourceEffect {
    data class TabClick(val value: Int) : OnlineTileSourceEffect

    data object NavigateBack : OnlineTileSourceEffect
    data object ToggleRangeSliderVisibility : OnlineTileSourceEffect
    data object TileSizeNotSelected : OnlineTileSourceEffect
}