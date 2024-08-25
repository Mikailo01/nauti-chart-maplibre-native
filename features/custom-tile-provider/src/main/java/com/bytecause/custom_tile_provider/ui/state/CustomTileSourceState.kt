package com.bytecause.custom_tile_provider.ui.state

import androidx.compose.material3.SnackbarHostState

data class CustomTileSourceState(
    val sourceName: String = "",
    val urlValue: String = "",
    val tileSize: Int = -1,
    val isLoading: Boolean = false,
    val isUrlValid: Boolean = true,
    val sourceNameError: TileNameError? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)