package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.RecentlySearchedPlace

data class SearchHistoryParentItem(val sectionTitle: String, val searchHistory: List<RecentlySearchedPlace>)
