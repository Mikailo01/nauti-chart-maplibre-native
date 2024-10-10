package com.bytecause.util.compose

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp


fun Dp.toPx(density: Density): Float =
    with(density) { toPx() }