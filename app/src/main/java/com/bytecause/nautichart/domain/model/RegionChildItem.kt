package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.data.local.room.tables.Region

data class RegionChildItem(
    val region: Region,
    val isChecked: Boolean,
    val isDownloading: Boolean,
    val isCheckBoxEnabled: Boolean,
    val isDownloaded: Boolean,
    val size: String
)
