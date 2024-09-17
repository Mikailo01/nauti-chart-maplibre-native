package com.bytecause.search.ui.model.serializable

import com.bytecause.domain.util.SearchTypes

data class PoiCategoryModel(
    val drawableId: Int,
    val name: Int,
    val search: SearchTypes
) : java.io.Serializable
