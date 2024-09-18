package com.bytecause.search.ui.model

data class SearchHistoryParentItem(
    val sectionTitle: String,
    val searchHistory: List<RecentlySearchedPlaceUiModel>
)
