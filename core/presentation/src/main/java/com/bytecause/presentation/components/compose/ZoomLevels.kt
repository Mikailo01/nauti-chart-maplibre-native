package com.bytecause.presentation.components.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bytecause.core.resources.R

@Composable
fun ZoomLevels(
    modifier: Modifier = Modifier,
    minZoom: Int,
    maxZoom: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.clickable {
            onClick()
        },
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.zoom_in),
            contentDescription = null
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.zoom_levels),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            AnnotatedString(
                textRes = R.string.min_max_zoom,
                minZoom.toString(),
                maxZoom.toString()
            )
        }
    }
}