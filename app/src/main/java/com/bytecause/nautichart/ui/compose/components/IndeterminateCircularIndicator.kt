package com.bytecause.nautichart.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    isShowed: Boolean,
    subContent: @Composable () -> Unit = { }
) {
    if (!isShowed) return

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(size)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            subContent()
        }
    }
}

@Composable
@Preview(showBackground = true)
fun IndeterminateCircularIndicatorPreview() {
    IndeterminateCircularIndicator(isShowed = true, size = 100.dp)
}