package com.bytecause.presentation.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bytecause.core.resources.R

@Composable
fun TileSizeChips(
    tileSize: Int,
    enabled: Boolean = true,
    onTileSizeValueChange: (Int) -> Unit
) {
    Column {
        Text(text = stringResource(id = R.string.tile_size), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = tileSize == 256,
                onClick = { onTileSizeValueChange(256) },
                label = { Text(text = "256x256") },
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_layers_24),
                        contentDescription = null
                    )
                }
            )
            FilterChip(
                selected = tileSize == 512,
                onClick = { onTileSizeValueChange(512) },
                label = { Text(text = "512x512") },
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_layers_24),
                        contentDescription = null
                    )
                })
        }
    }
}