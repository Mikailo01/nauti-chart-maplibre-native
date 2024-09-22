package com.bytecause.custom_tile_provider.ui.events

import com.bytecause.custom_tile_provider.ui.state.TileNameError

sealed interface OnlineTileSourceEvent {
    data class OnSourceNameChange(val value: String) : OnlineTileSourceEvent
    data class OnUrlValueChange(val value: String) : OnlineTileSourceEvent
    data class OnTileSizeValueChange(val value: Int) : OnlineTileSourceEvent
    data class OnLoadingValueChange(val value: Boolean) : OnlineTileSourceEvent
    data class OnUrlValidationChange(val value: Boolean) : OnlineTileSourceEvent
    data class OnTabClick(val value: Int) : OnlineTileSourceEvent
    data class OnSourceNameError(val error: TileNameError) : OnlineTileSourceEvent
    data class OnDoneButtonClick(val minZoom: Int, val maxZoom: Int) : OnlineTileSourceEvent
    data object OnTileSizeNotSelected : OnlineTileSourceEvent
    data object OnNavigateBack : OnlineTileSourceEvent
    data object OnToggleRangeSliderVisibility : OnlineTileSourceEvent
}

