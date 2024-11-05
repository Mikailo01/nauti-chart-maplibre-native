package com.bytecause.map.ui.state

import androidx.compose.runtime.Stable
import com.bytecause.map.ui.model.SortOptions

@Stable
data class TrackRouteChooseFilterState(
    val options: List<SortOptions> = SortOptions.entries,
    val selectedOption: SortOptions = SortOptions.Date
)