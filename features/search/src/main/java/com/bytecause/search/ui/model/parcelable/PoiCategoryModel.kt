package com.bytecause.search.ui.model.parcelable

import android.os.Parcelable
import com.bytecause.domain.util.SearchTypes
import com.bytecause.util.string.UiText
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PoiCategoryModel(
    val drawableId: Int,
    val name: Int,
    val search: @RawValue SearchTypes
): Parcelable
