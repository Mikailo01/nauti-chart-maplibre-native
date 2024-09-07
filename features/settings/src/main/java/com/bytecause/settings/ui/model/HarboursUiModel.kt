package com.bytecause.settings.ui.model

import com.bytecause.settings.ui.UpdateInterval

data class HarboursUiModel(
    val harboursUpdateInterval: UpdateInterval? = null,
    val timestamp: String = "",
    val isUpdating: Boolean = false,
    val progress: Int = -1
)