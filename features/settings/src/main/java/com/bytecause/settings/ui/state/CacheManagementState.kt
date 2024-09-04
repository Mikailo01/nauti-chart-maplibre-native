package com.bytecause.settings.ui.state

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.settings.ui.ConfirmationDialogType
import com.bytecause.settings.ui.UpdateInterval
import com.bytecause.settings.ui.model.RegionUiModel

@Immutable
data class CacheManagementState(
    val downloadedRegions: List<RegionUiModel> = emptyList(),
    val harboursTimestamp: String = "",
    val vesselsTimestamp: String = "",
    val harboursUpdateInterval: UpdateInterval? = null,
    val poiUpdateInterval: UpdateInterval? = null,
    val updatingRegionId: Int = -1,
    val progress: Int = -1,
    val showConfirmationDialog: ConfirmationDialogType? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)