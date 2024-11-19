package com.bytecause.domain.model

sealed class FilterOptions {
    data class Date(val value: DateFilterOptions) : FilterOptions()
    data class Distance(val value: DistanceFilterOptions) : FilterOptions()
    data class Duration(val value: DurationFilterOptions) : FilterOptions()
}

enum class DateFilterOptions {
    All,
    Today,
    Week,
    Month,
    Year
}

enum class DistanceFilterOptions(val value: Int) {
    All(-1),
    Short(10),
    Mid(50),
    Long(100),
    ExtraLong(100)
}

enum class DurationFilterOptions(val value: Long) {
    All(-1),
    OneHour(1),
    SixHours(6),
    TwelveHours(12),
    Day(1),
    MoreThanDay(1)
}