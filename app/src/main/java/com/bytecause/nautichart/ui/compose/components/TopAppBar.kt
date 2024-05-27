package com.bytecause.nautichart.ui.compose.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bytecause.nautichart.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ),
    navigationIcon: ImageVector? = null,
    actionIcon: @Composable () -> Unit = {},
    onNavigationIconClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        colors = colors,
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = { onNavigationIconClick() }) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Localized description",
                        tint = colors.titleContentColor
                    )
                }
            }
        },
        actions = {
            actionIcon()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TopAppBarPreview() {
    TopAppBar(titleRes = R.string.preview, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack) {}
}