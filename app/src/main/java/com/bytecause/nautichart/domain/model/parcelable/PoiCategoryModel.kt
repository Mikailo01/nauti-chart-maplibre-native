package com.bytecause.nautichart.domain.model.parcelable

import android.os.Parcelable
import com.bytecause.nautichart.util.SearchTypes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PoiCategoryModel(
    val drawableId: Int,
    val name: String,
    val search: @RawValue SearchTypes
): Parcelable
