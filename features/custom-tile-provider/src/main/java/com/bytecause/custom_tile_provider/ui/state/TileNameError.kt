package com.bytecause.custom_tile_provider.ui.state

sealed interface TileNameError {
    data object Empty : TileNameError
    data object Exists : TileNameError
}