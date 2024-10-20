package com.bytecause.map.ui.model

import com.bytecause.data.model.AnchorageHistoryDeletionIntervalModel

/**
 * Enum class representing the supported intervals for deleting anchorage history.
 *
 * Each enum constant corresponds to a specific time interval or state.
 *
 * @property displayText A string representing the number of days for which anchorage history will be kept,
 * or the infinity symbol (∞) for no deletion.
 *
 * The order of those enum constants must correspond to [AnchorageHistoryDeletionIntervalModel]
 */
enum class AnchorageHistoryDeletionInterval(val displayText: String) {

    /**
     * History will be retained for two weeks (14 days).
     * Corresponds to [AnchorageHistoryDeletionIntervalModel.TWO_WEEKS].
     */
    TWO_WEEKS("14"),

    /**
     * History will be retained for one month (30 days).
     * Corresponds to [AnchorageHistoryDeletionIntervalModel.MONTH].
     */
    MONTH("30"),

    /**
     * History will be kept indefinitely (no deletion).
     * Corresponds to [AnchorageHistoryDeletionIntervalModel.INFINITE].
     */
    INFINITE("∞");
}
