package com.bytecause.nautichart.ui.compose.components

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
    androidx.compose.material3.Divider(
        modifier = modifier,
        thickness = thickness.dp,
        color = color
    )
}