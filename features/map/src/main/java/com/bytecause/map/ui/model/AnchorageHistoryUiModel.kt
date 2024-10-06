package com.bytecause.map.ui.model

/**
 * [id] and [timestamp] have been omitted from custom [equals] and [hashCode] implementations, so this
 * object's usage must comply to their contract.
 */
data class AnchorageHistoryUiModel(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnchorageHistoryUiModel) return false

        return latitude == other.latitude &&
                longitude == other.longitude &&
                radius == other.radius
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + radius
        return result
    }
}