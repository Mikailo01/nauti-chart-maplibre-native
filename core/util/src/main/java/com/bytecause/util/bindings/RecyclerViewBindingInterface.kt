package com.bytecause.util.bindings

import android.view.View

interface RecyclerViewBindingInterface<T> {
    fun bindData(item: T, itemView: View, itemPosition: Int)
}