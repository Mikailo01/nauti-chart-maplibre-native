package com.bytecause.settings.ui.state

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.settings.ui.ConfirmationDialogType
import com.bytecause.settings.ui.UpdateInterval
import com.bytecause.settings.ui.model.HarboursUiModel
import com.bytecause.settings.ui.model.RegionUiModel

@Immutable
data class CacheManagementState(
    val downloadedRegions: Map<Int, RegionUiModel> = emptyMap(),
    val harboursModel: HarboursUiModel = HarboursUiModel(),
    val vesselsTimestamp: String = "",
    val poiUpdateInterval: UpdateInterval? = null,
    val showConfirmationDialog: ConfirmationDialogType? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)