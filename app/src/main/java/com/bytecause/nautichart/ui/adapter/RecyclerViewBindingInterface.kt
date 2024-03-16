package com.bytecause.nautichart.ui.adapter

import android.view.View

interface RecyclerViewBindingInterface<T> {
    fun bindData(item: T, itemView: View, itemPosition: Int)
}