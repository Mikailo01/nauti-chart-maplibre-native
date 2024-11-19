package com.bytecause.map.ui.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import com.bytecause.map.ui.model.AnchorageHistoryDeletionInterval
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.BottomSheetType

@Stable
data class AnchorageAlarmSettingsState(
    val maxGpsUpdateInterval: Int = 0,
    val minGpsUpdateInterval: Int = 0,
    val alarmDelay: Int = 0,
    val areAnchorageLocationsVisible: Boolean = false,
    val trackMovement: Boolean = true,
    val trackBatteryState: Boolean = false,
    val isEditMode: Boolean = false,
    val showDeleteHistoryConfirmationDialog: Boolean = false,
    val anchorageHistoryDeletionInterval: AnchorageHistoryDeletionInterval = AnchorageHistoryDeletionInterval.TWO_WEEKS,
    val bottomSheetType: BottomSheetType? = null,
    val anchorageHistory: List<AnchorageHistoryUiModel> = emptyList(),
    val lazyListState: LazyListState = LazyListState(),
    val hasAnimationFinished: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)