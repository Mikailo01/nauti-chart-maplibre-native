package com.bytecause.map.ui.model

import androidx.annotation.StringRes

sealed interface PoiCategory {
    data class PoiCategoryWithName(
       val name: String, val isSelected: Boolean
    ) : PoiCategory

    data class PoiCategoryWithNameRes(
        @StringRes val nameRes: Int,
        val isSelected: Boolean
    ) : PoiCategory
}

//data class PoiCategory(@StringRes val nameRes: Int?, val name: String?, val isSelected: Boolean)