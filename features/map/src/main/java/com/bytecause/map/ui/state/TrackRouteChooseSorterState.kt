package com.bytecause.map.ui.state

import androidx.compose.runtime.Stable
import com.bytecause.domain.model.SortOptions

@Stable
data class TrackRouteChooseSorterState(
    val selectedOption: SortOptions = SortOptions.Recent
)