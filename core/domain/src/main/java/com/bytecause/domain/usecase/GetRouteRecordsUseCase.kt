package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.TrackRouteRepository
import com.bytecause.domain.model.DateFilterOptions
import com.bytecause.domain.model.DistanceFilterOptions
import com.bytecause.domain.model.DurationFilterOptions
import com.bytecause.domain.model.RouteRecordModel
import com.bytecause.domain.model.SortOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class GetRouteRecordsUseCase(private val repository: TrackRouteRepository) {

    operator fun invoke(
        sorter: SortOptions,
        filter1: DateFilterOptions,
        filter2: DistanceFilterOptions,
        filter3: DurationFilterOptions
    ): Flow<List<RouteRecordModel>> =
        repository.getRecords()
            .map { records ->
                records
                    .filter { record ->
                        isWithinDateRange(record.dateCreated, filter1)
                    }
                    .filter { record ->
                        isWithinDistanceRange(record.distance, filter2)
                    }
                    .filter { record ->
                        isWithinDurationFilter(record.dateCreated - record.startTime, filter3)
                    }
                    .sortedWith(
                        when (sorter) {
                            SortOptions.Name -> compareBy { it.name.lowercase() }
                            SortOptions.Recent -> compareByDescending { it.dateCreated }
                            SortOptions.Distance -> compareBy { it.distance }
                            SortOptions.Duration -> compareBy { it.dateCreated - it.startTime }
                        }
                    )
            }

    private fun isWithinDateRange(timestamp: Long, filterOption: DateFilterOptions): Boolean {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())

        return when (filterOption) {
            DateFilterOptions.All -> true
            DateFilterOptions.Today -> date == today
            DateFilterOptions.Week -> date.isAfter(today.minusWeeks(1)) || date.isEqual(
                today.minusWeeks(
                    1
                )
            )

            DateFilterOptions.Month -> date.isAfter(today.minusMonths(1)) || date.isEqual(
                today.minusMonths(
                    1
                )
            )

            DateFilterOptions.Year -> date.isAfter(today.minusYears(1)) || date.isEqual(
                today.minusYears(
                    1
                )
            )
        }
    }

    private fun isWithinDistanceRange(
        distance: Double,
        filterOptions: DistanceFilterOptions
    ): Boolean {
        return when (filterOptions) {
            DistanceFilterOptions.All -> true
            DistanceFilterOptions.ExtraLong -> distance > filterOptions.value
            else -> distance <= filterOptions.value
        }
    }

    private fun isWithinDurationFilter(
        duration: Long,
        filterOptions: DurationFilterOptions
    ): Boolean {
        return when (filterOptions) {
            DurationFilterOptions.All -> true
            DurationFilterOptions.Day -> duration <= Duration.ofDays(filterOptions.value)
                .toMillis()

            DurationFilterOptions.MoreThanDay -> duration > Duration.ofDays(filterOptions.value)
                .toMillis()

            else -> duration <= Duration.ofHours(filterOptions.value).toMillis()
        }
    }
}