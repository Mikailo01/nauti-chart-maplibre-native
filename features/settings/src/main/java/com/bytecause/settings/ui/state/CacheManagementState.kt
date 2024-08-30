package com.bytecause.settings.ui.state

import androidx.compose.material3.SnackbarHostState

data class CacheManagementState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)