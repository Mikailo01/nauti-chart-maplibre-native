package com.bytecause.custom_tile_provider.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.core.resources.R
import com.bytecause.custom_tile_provider.ui.events.OnlineTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.OnlineTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.OnlineTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.custom_tile_provider.ui.viewmodel.OnlineTileSourceViewModel
import com.bytecause.presentation.components.compose.CustomOutlinedButton
import com.bytecause.presentation.components.compose.CustomRangeSlider
import com.bytecause.presentation.components.compose.Divider
import com.bytecause.presentation.components.compose.TileSizeChips
import com.bytecause.presentation.components.compose.ZoomLevels
import kotlinx.coroutines.launch


private const val MIN_ZOOM = 0f
private const val MAX_ZOOM = 24f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineTileSourceScreen(
    viewModel: OnlineTileSourceViewModel = hiltViewModel(),
    pagerState: PagerState,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    val rangeSliderState = remember {
        RangeSliderState(
            activeRangeStart = MIN_ZOOM,
            activeRangeEnd = MAX_ZOOM,
            steps = (MAX_ZOOM - 1f).toInt(),
            valueRange = MIN_ZOOM.rangeTo(MAX_ZOOM)
        )
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnlineTileSourceEffect.TabClick -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(effect.value)
                    }
                }

                OnlineTileSourceEffect.NavigateBack -> onNavigateBack()
                OnlineTileSourceEffect.TileSizeNotSelected -> {
                    state.snackbarHostState.showSnackbar(context.getString(R.string.tile_size_not_selected))
                }

                OnlineTileSourceEffect.ToggleRangeSliderVisibility -> {
                    coroutineScope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        } else bottomSheetScaffoldState.bottomSheetState.show()
                    }
                }
            }
        }
    }

    OnlineTileSourceContent(
        state = state,
        minZoom = rangeSliderState.activeRangeStart.toInt(),
        maxZoom = rangeSliderState.activeRangeEnd.toInt(),
        rangeSliderState = rangeSliderState,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        onEvent = viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineTileSourceContent(
    state: OnlineTileSourceState,
    minZoom: Int,
    maxZoom: Int,
    rangeSliderState: RangeSliderState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onEvent: (OnlineTileSourceEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextField(
                value = state.sourceName,
                onValueChange = { onEvent(OnlineTileSourceEvent.OnSourceNameChange(it)) },
                label = {
                    Text(
                        text = stringResource(id = R.string.name)
                    )
                },
                isError = state.sourceNameError != null,
                supportingText = {
                    if (state.sourceNameError != null) {
                        when (state.sourceNameError) {
                            TileNameError.Empty -> Text(text = stringResource(id = R.string.name_cannot_be_empty))
                            TileNameError.Exists -> Text(text = stringResource(id = R.string.name_of_this_tile_provider_already_exists))
                        }
                    }
                }
            )
            TextField(
                value = state.urlValue,
                onValueChange = { onEvent(OnlineTileSourceEvent.OnUrlValueChange(it)) },
                isError = !state.isUrlValid,
                label = {
                    Text(
                        text = "Url"
                    )
                },
                supportingText = {
                    if (!state.isUrlValid) {
                        when {
                            state.urlValue.isBlank() -> {
                                Text(text = stringResource(id = R.string.url_cannot_be_empty))
                            }

                            else -> {
                                Text(
                                    text = stringResource(id = R.string.url_is_not_in_valid_format).format(
                                        stringResource(id = R.string.url_format_1),
                                        stringResource(id = R.string.url_format_2)
                                    )
                                )
                            }
                        }
                    }
                }
            )
            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            ZoomLevels(
                minZoom = minZoom,
                maxZoom = maxZoom,
                onClick = {
                    onEvent(OnlineTileSourceEvent.OnToggleRangeSliderVisibility)
                }
            )

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            TileSizeChips(tileSize = state.tileSize) {
                onEvent(OnlineTileSourceEvent.OnTileSizeValueChange(it))
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
                    onClick = { onEvent(OnlineTileSourceEvent.OnDoneButtonClick(minZoom, maxZoom)) }
                )
            }
        }
        BottomSheetScaffold(
            modifier = Modifier.align(Alignment.BottomCenter),
            sheetContent = {
                CustomRangeSlider(
                    modifier = Modifier.padding(15.dp),
                    rangeSliderState = rangeSliderState,
                    showIndicator = true,
                    showLabel = true,
                )
            },
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 150.dp
        ) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun OnlineTileSourceContentPreview() {
    OnlineTileSourceContent(
        state = OnlineTileSourceState(),
        minZoom = 0,
        maxZoom = 24,
        rangeSliderState = remember {
            RangeSliderState()
        },
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        onEvent = {},
    )
}