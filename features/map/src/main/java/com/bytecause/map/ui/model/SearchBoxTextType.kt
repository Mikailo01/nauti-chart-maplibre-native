package com.bytecause.map.ui.model

sealed interface SearchBoxTextType {
    data class Coordinates(val text: String = "") : SearchBoxTextType
    data class PoiName(val text: String = "") : SearchBoxTextType
}