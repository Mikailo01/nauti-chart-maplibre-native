package com.bytecause.pois.ui.recyclerview.interfaces

import android.widget.CompoundButton

interface CountryAndRegionsListener {
    fun onCheckBoxClickListener(
        buttonView: CompoundButton,
        position: Int,
        parentPosition: Int,
        isChecked: Boolean,
    )

    fun onExpandClickListener(position: Int)

    fun onCountryDownloadClickListener(position: Int)
}