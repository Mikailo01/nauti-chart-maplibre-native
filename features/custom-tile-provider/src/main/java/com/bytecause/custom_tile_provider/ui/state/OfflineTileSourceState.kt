package com.bytecause.custom_tile_provider.ui.state

import androidx.compose.material3.SnackbarHostState

data class OfflineTileSourceState(
    val sourceName: String = "",
    val tileSize: Int = -1,
    val isLoading: Boolean = false,
    val fileUri: String? = null,
    val sourceNameError: TileNameError? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
)