package com.bytecause.map.ui.model

sealed class MeasureUnit {
    data class Meters(val value: Int) : MeasureUnit()
    data class KiloMeters(val value: Double) : MeasureUnit()
}