package com.bytecause.settings.ui.event

sealed interface CacheManagementEvent {
    data object OnNavigateBack : CacheManagementEvent
}

sealed interface CacheManagementEffect {
    data object NavigateBack : CacheManagementEffect
}