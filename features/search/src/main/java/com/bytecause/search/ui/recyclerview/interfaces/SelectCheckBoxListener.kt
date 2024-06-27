package com.bytecause.search.ui.recyclerview.interfaces

import android.widget.CompoundButton

interface SelectCheckBoxListener {
    fun onCheckBoxClickListener(
        buttonView: CompoundButton,
        position: Int,
        parentPosition: Int,
        isChecked: Boolean,
    )
}