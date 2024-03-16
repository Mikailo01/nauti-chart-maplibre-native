package com.bytecause.nautichart.domain.model

import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes


data class LayersChildItem(
    val type: LayerTypes,
    val drawableId: Int,
    val resourceNameId: Int
)