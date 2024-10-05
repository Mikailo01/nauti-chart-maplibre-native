package com.bytecause.util.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp


@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.thenInternal(
    condition: Boolean,
    onTrue: @Composable (Modifier.() -> Modifier)? = null,
    onFalse: @Composable (Modifier.() -> Modifier)? = null
) = (if (condition) {
    onTrue?.let { composed { then(Modifier.it()) } }
} else {
    onFalse?.let { composed { then(Modifier.it()) } }
}) ?: this

fun Modifier.then(
    condition: Boolean,
    onTrue: @Composable (Modifier.() -> Modifier)? = null,
    onFalse: @Composable (Modifier.() -> Modifier)? = null
) = thenInternal(condition, onTrue, onFalse)

fun Dp.px(density: Density): Float =
    with(density) { toPx() }