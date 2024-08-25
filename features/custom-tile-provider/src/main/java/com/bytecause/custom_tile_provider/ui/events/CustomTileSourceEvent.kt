package com.bytecause.custom_tile_provider.ui.events

import com.bytecause.custom_tile_provider.ui.state.TileNameError

sealed interface CustomTileSourceEvent {
    data class OnSourceNameChange(val value: String) : CustomTileSourceEvent
    data class OnUrlValueChange(val value: String) : CustomTileSourceEvent
    data class OnTileSizeValueChange(val value: Int) : CustomTileSourceEvent
    data class OnLoadingValueChange(val value: Boolean) : CustomTileSourceEvent
    data class OnUrlValidationChange(val value: Boolean) : CustomTileSourceEvent
    data class OnTabClick(val value: Int) : CustomTileSourceEvent
    data class OnSourceNameError(val error: TileNameError) : CustomTileSourceEvent
    data object OnTileSizeNotSelected : CustomTileSourceEvent
    data object OnNavigateBack : CustomTileSourceEvent
    data object OnLaunchFileManager : CustomTileSourceEvent
    data object OnDoneButtonClick : CustomTileSourceEvent
    data object OnToggleRangeSliderVisibility : CustomTileSourceEvent
}

sealed interface CustomTileSourceEffect {
    data class TabClick(val value: Int) : CustomTileSourceEffect

    data object NavigateBack : CustomTileSourceEffect
    data object LaunchFileManager : CustomTileSourceEffect
    data object DoneButtonClick : CustomTileSourceEffect
    data object ToggleRangeSliderVisibility : CustomTileSourceEffect
    data object TileSizeNotSelected : CustomTileSourceEffect
}