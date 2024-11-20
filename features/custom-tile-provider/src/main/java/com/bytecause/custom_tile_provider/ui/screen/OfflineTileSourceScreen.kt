package com.bytecause.custom_tile_provider.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.core.resources.R
import com.bytecause.custom_tile_provider.ui.events.OfflineTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.OfflineTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.OfflineTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.custom_tile_provider.ui.viewmodel.OfflineTileSourceViewModel
import com.bytecause.presentation.components.compose.ConfirmationDialog
import com.bytecause.presentation.components.compose.CustomOutlinedButton
import com.bytecause.presentation.components.compose.Divider
import com.bytecause.presentation.components.compose.TileSizeChips
import com.bytecause.util.file.FileUtil.queryName
import com.spr.jetpack_loading.components.indicators.BallPulseRiseIndicator
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun OfflineTileSourceScreen(
    viewModel: OfflineTileSourceViewModel = hiltViewModel(),
    pagerState: PagerState,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        if (queryName(context.contentResolver, uri)?.endsWith(".mbtiles") == false) {
            viewModel.uiEventHandler(OfflineTileSourceEvent.OnFileUriSelected(null))
            coroutineScope.launch {
                state.snackbarHostState.showSnackbar(context.getString(R.string.invalid_file_type))
            }
        } else {
            viewModel.uiEventHandler(OfflineTileSourceEvent.OnFileUriSelected(uri.toString()))
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            coroutineScope.cancel()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OfflineTileSourceEffect.ShowMessage -> {
                    state.snackbarHostState.showSnackbar(
                        context.getString(effect.messageId),
                        duration = SnackbarDuration.Long
                    )
                }

                is OfflineTileSourceEffect.TabClick -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(effect.value)
                    }
                }

                OfflineTileSourceEffect.LaunchFileManager -> launcher.launch("application/octet-stream")
                OfflineTileSourceEffect.NavigateBack -> onNavigateBack()
                OfflineTileSourceEffect.TileSizeNotSelected -> {
                    state.snackbarHostState.showSnackbar(context.getString(R.string.tile_size_not_selected))
                }

                OfflineTileSourceEffect.VectorUnsupported -> {
                    state.snackbarHostState.showSnackbar("Vector tiles are not currently supported.")
                }
            }
        }
    }

    OfflineTileSourceContent(
        state = state,
        onEvent = viewModel::uiEventHandler
    )
}

@Composable
fun OfflineTileSourceContent(
    state: OfflineTileSourceState,
    onEvent: (OfflineTileSourceEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextField(
                value = state.sourceName,
                onValueChange = { onEvent(OfflineTileSourceEvent.OnSourceNameChange(it)) },
                label = {
                    Text(
                        text = stringResource(id = R.string.name)
                    )
                },
                enabled = !state.isLoading,
                isError = state.sourceNameError != null,
                supportingText = {
                    if (state.sourceNameError is TileNameError.Empty) Text(
                        text = stringResource(
                            id = R.string.name_cannot_be_empty
                        )
                    )
                }
            )

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            TileSizeChips(
                tileSize = state.tileSize,
                enabled = !state.isLoading
            ) {
                onEvent(OfflineTileSourceEvent.OnTileSizeValueChange(it))
            }

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            Button(
                onClick = { onEvent(OfflineTileSourceEvent.OnLaunchFileManager) },
                enabled = state.tileSize != -1 && !state.isLoading
            ) {
                Text(text = stringResource(id = R.string.choose_mbtile))
            }

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CustomOutlinedButton(
                    text = stringResource(id = R.string.done),
                    onClick = { onEvent(OfflineTileSourceEvent.OnDoneButtonClick) }
                )
            }
        }

        SnackbarHost(
            hostState = state.snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        if (state.sourceNameError is TileNameError.Exists) {
            ConfirmationDialog(
                title = R.string.name_of_this_tile_provider_already_exists,
                confirmText = R.string.overwrite,
                dismissText = R.string.cancel,
                onConfirm = { onEvent(OfflineTileSourceEvent.OnTileSourceOverwriteDialogConfirm) },
                onDismiss = { onEvent(OfflineTileSourceEvent.OnTileSourceOverwriteDialogDismiss) }
            ) {
                Text(
                    text = stringResource(R.string.do_you_wish_to_overwrite_an_existing_tile_source),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                BallPulseRiseIndicator(
                    color = colorResource(id = R.color.md_theme_primary),
                    ballDiameter = 48f
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun OfflineTileSourceContentPreview() {
    OfflineTileSourceContent(
        state = OfflineTileSourceState(),
        onEvent = {}
    )
}