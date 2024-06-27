package com.bytecause.presentation.components.compose

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    thickness: Int,
    color: Color
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness.dp,
        color = color
    )
}