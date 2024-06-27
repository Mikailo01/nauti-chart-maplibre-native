package com.bytecause.search.ui.recyclerview.interfaces

import android.view.View

interface SearchHistoryAdapterListener {
    fun onItemViewClickListener(
        view: View,
        parentIndex: Int,
        childIndex: Int,
    )
}