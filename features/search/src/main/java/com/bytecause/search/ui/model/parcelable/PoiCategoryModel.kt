package com.bytecause.search.ui.model.parcelable

import android.os.Parcelable
import com.bytecause.domain.model.SearchTypes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PoiCategoryModel(
    val drawableId: Int,
    val name: String,
    val search: @RawValue SearchTypes
): Parcelable
