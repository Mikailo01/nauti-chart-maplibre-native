package com.bytecause.custom_tile_provider.ui.events

import androidx.annotation.StringRes

sealed interface OfflineTileSourceEffect {
    data class TabClick(val value: Int) : OfflineTileSourceEffect
    data class ShowMessage(@StringRes val messageId: Int) : OfflineTileSourceEffect

    data object NavigateBack : OfflineTileSourceEffect
    data object LaunchFileManager : OfflineTileSourceEffect
    data object TileSizeNotSelected : OfflineTileSourceEffect
    data object VectorUnsupported: OfflineTileSourceEffect
}