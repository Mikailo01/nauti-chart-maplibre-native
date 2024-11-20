package com.bytecause.custom_tile_provider.ui.events

import com.bytecause.custom_tile_provider.ui.state.TileNameError

sealed interface OfflineTileSourceEvent {
    data class OnSourceNameChange(val value: String) : OfflineTileSourceEvent
    data class OnTileSizeValueChange(val value: Int) : OfflineTileSourceEvent
    data class OnLoadingValueChange(val value: Boolean) : OfflineTileSourceEvent
    data class OnTabClick(val value: Int) : OfflineTileSourceEvent
    data class OnSourceNameError(val error: TileNameError) : OfflineTileSourceEvent
    data class OnFileUriSelected(val value: String?) : OfflineTileSourceEvent
    data object OnTileSizeNotSelected : OfflineTileSourceEvent
    data object OnNavigateBack : OfflineTileSourceEvent
    data object OnLaunchFileManager : OfflineTileSourceEvent
    data object OnDoneButtonClick : OfflineTileSourceEvent
    data object OnVectorUnsupported : OfflineTileSourceEvent
    data object OnTileSourceOverwriteDialogDismiss : OfflineTileSourceEvent
    data object OnTileSourceOverwriteDialogConfirm : OfflineTileSourceEvent
}