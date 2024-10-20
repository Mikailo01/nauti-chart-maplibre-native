package com.bytecause.data.model

enum class AnchorageHistoryDeletionIntervalModel(val interval: Long) {
    TWO_WEEKS(1_209_600_000),
    MONTH(2_592_000_000),
    INFINITE(-1L);
}