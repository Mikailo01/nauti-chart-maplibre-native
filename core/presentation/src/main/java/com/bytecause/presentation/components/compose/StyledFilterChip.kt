package com.bytecause.presentation.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

@Composable
fun StyledFilterChip(
    isSelected: Boolean,
    enabled: Boolean = true,
    label: @Composable () -> Unit = {},
    leadingIcon: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = label,
        enabled = enabled,
        leadingIcon = leadingIcon,
        border = BorderStroke(
            2.dp,
            colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
        ),
        colors = FilterChipDefaults.filterChipColors().copy(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedContainerColor = MaterialTheme.colorScheme.inversePrimary
        )
    )
}