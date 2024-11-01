package com.bytecause.map.ui.model

sealed class MeasureUnit {
    data class Meters(val value: Int) : MeasureUnit()
    data class KiloMeters(val value: Double) : MeasureUnit()
    data class NauticalMiles(val value: Double) : MeasureUnit()
}

enum class MetersUnitConvertConstants(val value: Int) {
    KiloMeters(1000),
    NauticalMiles(1852)
}