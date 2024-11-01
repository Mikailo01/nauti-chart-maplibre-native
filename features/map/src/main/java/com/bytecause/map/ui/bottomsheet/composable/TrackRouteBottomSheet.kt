package com.bytecause.map.ui.bottomsheet.composable

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.core.resources.R
import com.bytecause.data.services.Actions
import com.bytecause.map.services.TrackRouteService
import com.bytecause.map.ui.effect.TrackRouteBottomSheetEffect
import com.bytecause.map.ui.event.TrackRouteBottomSheetEvent
import com.bytecause.map.ui.model.TrackedRouteItem
import com.bytecause.map.ui.state.TrackRouteState
import com.bytecause.map.ui.viewmodel.MapViewModel
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.common.formatDuration
import com.bytecause.util.common.getDateTimeFromTimestamp
import com.bytecause.util.compose.swipeToDismiss
import com.bytecause.util.compose.then
import kotlinx.coroutines.delay

@Composable
fun TrackRoute(viewModel: MapViewModel, onCloseBottomSheet: () -> Unit) {
    val state by viewModel.trackRouteState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.trackRouteEffect.collect { effect ->
            when (effect) {
                TrackRouteBottomSheetEffect.StartForegroundService -> {
                    Intent(context, TrackRouteService::class.java).apply {
                        setAction(Actions.START.toString())
                        context.startService(this)
                    }
                }

                TrackRouteBottomSheetEffect.StopForegroundService -> {
                    Intent(context, TrackRouteService::class.java).apply {
                        setAction(Actions.STOP.toString())
                        context.startService(this)
                    }
                }

                TrackRouteBottomSheetEffect.CloseBottomSheet -> onCloseBottomSheet()
            }
        }
    }

    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            TrackRouteContent(
                state = state,
                onEvent = viewModel::trackRouteBottomSheetEventHandler
            )
        }
    }
}

@Composable
private fun TrackRouteContent(
    state: TrackRouteState,
    onEvent: (TrackRouteBottomSheetEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tracks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { onEvent(TrackRouteBottomSheetEvent.OnToggleEditMode) },
                colors = if (state.isEditMode) {
                    IconButtonDefaults.iconButtonColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                } else IconButtonDefaults.iconButtonColors()
            ) {
                Image(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
            IconButton(
                onClick = { onEvent(TrackRouteBottomSheetEvent.OnFilterClick) },
                colors = IconButtonDefaults.iconButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Image(
                    painter = painterResource(R.drawable.filter_off_icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
            IconButton(
                onClick = { onEvent(TrackRouteBottomSheetEvent.OnSortClick) },
                colors = IconButtonDefaults.iconButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Image(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
        }
        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(start = 20.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(state.records, key = { _, item -> item.id }) { index, item ->
                RecordItem(
                    item = item,
                    isEditModeEnabled = state.isEditMode,
                    index = index,
                    onClick = { onEvent(TrackRouteBottomSheetEvent.OnItemClick(it)) },
                    onRemove = { onEvent(TrackRouteBottomSheetEvent.OnRemoveItem(it)) }
                )
                HorizontalDivider()
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = state.isRenderAllTracksSwitchChecked,
                onCheckedChange = { onEvent(TrackRouteBottomSheetEvent.OnToggleRenderAllTracksSwitch) },
                colors = SwitchDefaults.colors(),
            )
            Text(
                text = "Render all tracks",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onEvent(
                        if (state.serviceRunning) TrackRouteBottomSheetEvent.OnStopForegroundService
                        else TrackRouteBottomSheetEvent.OnStartForegroundService
                    )
                },
                colors = if (state.serviceRunning) {
                    IconButtonDefaults.iconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    IconButtonDefaults.iconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            ) {
                Image(
                    painter = painterResource(
                        if (state.serviceRunning) R.drawable.baseline_stop_24
                        else R.drawable.baseline_play_arrow_24
                    ),
                    contentDescription = null
                )
            }

            OutlinedButton(onClick = { onEvent(TrackRouteBottomSheetEvent.OnCloseBottomSheet) }) {
                Text(text = stringResource(R.string.close))
            }
        }
    }
}

@Composable
private fun RecordItem(
    item: TrackedRouteItem,
    isEditModeEnabled: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    // Animatable for horizontal offset
    val offsetX = remember {
        Animatable(0f)
    }

    var hasFinished by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(isEditModeEnabled) {
        if (isEditModeEnabled && hasFinished.not()) {
            delay(100L * index) // animate incrementally

            offsetX.animateTo(
                targetValue = 100f,
                animationSpec = tween(durationMillis = 300)
            )

            delay(100L)

            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )

            hasFinished = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.toInt(), 0) }
            .then(
                condition = isEditModeEnabled,
                onTrue = {
                    swipeToDismiss { onRemove(item.id) }
                },
                onFalse = { clickable { onClick(item.id) } }
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = getDateTimeFromTimestamp(item.dateCreated),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = FontStyle.Italic
            )
        }

        Column {
            Text(text = stringResource(R.string.distance) + ": ${item.distance} NM")
            Text(text = "Duration" + ": ${formatDuration(item.duration)}")
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TrackRouteContentPreview() {
    TrackRouteContent(
        state = TrackRouteState(
            records = listOf(
                TrackedRouteItem(
                    id = 0,
                    name = "D",
                    description = "",
                    distance = 0.0,
                    dateCreated = 1600616546521L,
                    duration = 6050000L
                ),
                TrackedRouteItem(
                    id = 1,
                    name = "D",
                    description = "",
                    distance = 0.0,
                    dateCreated = 1600616546521L,
                    duration = 0L
                ),
                TrackedRouteItem(
                    id = 2,
                    name = "D",
                    description = "",
                    distance = 0.0,
                    dateCreated = 1600616546521L,
                    duration = 0L
                ),
            )
        ),
        onEvent = {}
    )
}