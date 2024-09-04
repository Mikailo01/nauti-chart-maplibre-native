package com.bytecause.settings.ui.event

import com.bytecause.settings.ui.ConfirmationDialogType
import com.bytecause.settings.ui.UpdateInterval

sealed interface CacheManagementEvent {
    data class OnShowConfirmationDialog(val value: ConfirmationDialogType?) : CacheManagementEvent
    data class OnDeleteRegion(val regionId: Int) : CacheManagementEvent
    data class OnUpdateRegion(val regionId: Int) : CacheManagementEvent
    data class OnSetPoiUpdateInterval(val interval: UpdateInterval) : CacheManagementEvent
    data class OnSetHarboursUpdateInterval(val interval: UpdateInterval) : CacheManagementEvent
    data object OnUpdateHarbours : CacheManagementEvent
    data object OnCancelRegionUpdate : CacheManagementEvent
    data object OnNavigateBack : CacheManagementEvent
    data object OnClearSearchHistory : CacheManagementEvent
    data object OnClearVessels : CacheManagementEvent
    data object OnClearHarbours : CacheManagementEvent
}

sealed interface CacheManagementEffect {
    data object NavigateBack : CacheManagementEffect
    data object RegionUpdateFailure : CacheManagementEffect
    data object RegionUpdateSuccess : CacheManagementEffect
}