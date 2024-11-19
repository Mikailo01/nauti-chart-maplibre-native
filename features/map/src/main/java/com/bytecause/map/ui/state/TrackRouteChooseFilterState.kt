package com.bytecause.map.ui.state

import androidx.compose.runtime.Stable
import com.bytecause.domain.model.DateFilterOptions
import com.bytecause.domain.model.DistanceFilterOptions
import com.bytecause.domain.model.DurationFilterOptions

@Stable
data class TrackRouteChooseFilterState(
    val selectedDateFilterOption: DateFilterOptions = DateFilterOptions.All,
    val selectedDistanceFilterOption: DistanceFilterOptions = DistanceFilterOptions.All,
    val selectedDurationFilterOption: DurationFilterOptions = DurationFilterOptions.All
)