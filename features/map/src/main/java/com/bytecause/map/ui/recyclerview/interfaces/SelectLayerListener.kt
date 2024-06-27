package com.bytecause.map.ui.recyclerview.interfaces

import android.view.View

interface SelectLayerListener {
    fun onItemViewClickListener(
        view: View,
        position: Int
    )

    fun onItemViewLongClickListener(
        view: View,
        parentPosition: Int,
        childPosition: Int
    )
}