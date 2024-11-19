package com.bytecause.map.ui.bottomsheet.composable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.core.resources.R
import com.bytecause.data.services.Actions
import com.bytecause.domain.model.DateFilterOptions
import com.bytecause.domain.model.DistanceFilterOptions
import com.bytecause.domain.model.DurationFilterOptions
import com.bytecause.domain.model.FilterOptions
import com.bytecause.domain.model.SortOptions
import com.bytecause.map.services.TrackRouteService
import com.bytecause.map.ui.dialog.ConfirmStopRouteTrackComposeDialog
import com.bytecause.map.ui.effect.TrackRouteBottomSheetEffect
import com.bytecause.map.ui.event.TrackRouteBottomSheetEvent
import com.bytecause.map.ui.model.RouteRecordUiModel
import com.bytecause.map.ui.model.TrackRouteContentType
import com.bytecause.map.ui.model.TrackedRouteItem
import com.bytecause.map.ui.state.TrackRouteChooseFilterState
import com.bytecause.map.ui.state.TrackRouteChooseSorterState
import com.bytecause.map.ui.state.TrackRouteMainContentState
import com.bytecause.map.ui.viewmodel.MapViewModel
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.common.formatDuration
import com.bytecause.util.common.getDateTimeFromTimestamp
import com.bytecause.util.compose.swipeToDismiss
import com.bytecause.util.compose.then
import com.bytecause.util.context.getActivity
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrackRoute(viewModel: MapViewModel, onCloseBottomSheet: () -> Unit) {
    val mainContentState by viewModel.trackRouteMainContentState.collectAsStateWithLifecycle()
    val chooseFilterState by viewModel.trackRouteChooseFilterState.collectAsStateWithLifecycle()
    val chooseSorterState by viewModel.trackRouteChooseSorterState.collectAsStateWithLifecycle()
    val contentType by viewModel.trackRouteBottomSheetContentType.collectAsStateWithLifecycle()

    val records by viewModel.getTrackedRecords.collectAsStateWithLifecycle()
    val routeRecord by viewModel.routeRecords.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val offsetMapX = remember {
        mutableStateMapOf<Long, Animatable<Float, AnimationVector1D>>()
    }

    LaunchedEffect(Unit) {
        viewModel.trackRouteEffect.collect { effect ->
            when (effect) {
                TrackRouteBottomSheetEffect.StartForegroundService -> {
                    Intent(context, TrackRouteService::class.java).apply {
                        action = Actions.START.toString()
                        context.startService(this)
                    }
                }

                TrackRouteBottomSheetEffect.CloseBottomSheet -> onCloseBottomSheet()

                TrackRouteBottomSheetEffect.ShowConfirmStopRouteTrackDialog -> {
                    (context.getActivity() as? AppCompatActivity)?.supportFragmentManager?.let {
                        ConfirmStopRouteTrackComposeDialog().run {
                            show(it, tag)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(mainContentState.isEditMode) {
        if (mainContentState.isEditMode && mainContentState.hasAnimationFinished.not()) {
            for (item in mainContentState.lazyListState.layoutInfo.visibleItemsInfo) {
                launch {
                    delay(100L * item.index)

                    val itemKey = item.key as Long

                    offsetMapX[itemKey] = Animatable(0f)

                    offsetMapX[itemKey]?.animateTo(
                        targetValue = 100f,
                        animationSpec = tween(durationMillis = 300)
                    )

                    delay(100L)

                    offsetMapX[itemKey]?.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }

            viewModel.trackRouteBottomSheetMainContentEventHandler(
                TrackRouteBottomSheetEvent.OnAnimationFinished
            )
        }
    }

    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            when (contentType) {
                is TrackRouteContentType.Detail -> {
                    TrackRouteDetailContent(
                        item = routeRecord.first(),
                        onShowSpeedPoints = { speedValue -> viewModel.findSpeedPoints(speedValue) },
                        onCloseBottomSheet = onCloseBottomSheet,
                        onNavigateBack = { viewModel.clearRouteRecord() }
                    )
                }

                TrackRouteContentType.Filter -> {
                    TrackRouteChooseFilterContent(
                        state = chooseFilterState,
                        onRadioButtonClick = viewModel::updateFilterOption,
                        onNavigateBack = { viewModel.resetContentTypeToDefault() })
                }

                TrackRouteContentType.Main -> {
                    TrackRouteMainContent(
                        state = mainContentState,
                        records = records,
                        offsetX = { offsetMapX.mapValues { it.value.value } },
                        onEvent = viewModel::trackRouteBottomSheetMainContentEventHandler
                    )
                }

                TrackRouteContentType.Sort -> {
                    TrackRouteChooseSorterContent(
                        state = chooseSorterState,
                        onRadioButtonClick = viewModel::updateSortOption,
                        onNavigateBack = { viewModel.resetContentTypeToDefault() })
                }
            }
        }
    }
}

@Composable
private fun TrackRouteMainContent(
    state: TrackRouteMainContentState,
    records: List<TrackedRouteItem>,
    offsetX: () -> Map<Long, Float>,
    onEvent: (TrackRouteBottomSheetEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = stringResource(R.string.tracks),
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
                    contentDescription = stringResource(R.string.edit_mode),
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
                    contentDescription = stringResource(R.string.filter),
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
                    contentDescription = stringResource(R.string.sort),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
        }

        HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(start = 20.dp))

        LazyColumn(
            state = state.lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(records, key = { item -> item.id }) { item ->

                RecordItem(
                    item = item,
                    isEditModeEnabled = state.isEditMode,
                    offsetX = { offsetX()[item.id] ?: 0f },
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
                text = stringResource(R.string.show_all_tracks),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    onEvent(
                        if (state.serviceRunning) TrackRouteBottomSheetEvent.OnShowConfirmStopRouteTrackDialog
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
private fun TrackRouteDetailContent(
    item: RouteRecordUiModel,
    onShowSpeedPoints: (Double) -> Unit,
    onCloseBottomSheet: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = stringResource(R.string.name) + ": ${item.name}")
                if (item.description.isNotBlank()) {
                    Text(text = stringResource(R.string.description) + ": ${item.description}")
                }
                Text(text = stringResource(R.string.duration) + ": ${formatDuration(item.duration)}")
                Text(text = stringResource(R.string.start_time) + ": ${getDateTimeFromTimestamp(item.startTime)}")
                Text(text = stringResource(R.string.end_time) + ": ${getDateTimeFromTimestamp(item.dateCreated)}")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (item.speed.values.isNotEmpty()) {
            LineChart(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                data = remember {
                    listOf(
                        Line(
                            label = context.getString(R.string.speed),
                            values = item.speed.values.map { it.toDouble() },
                            color = SolidColor(color),
                            firstGradientFillColor = color.copy(alpha = 0.5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(1000, easing = EaseInOutCubic),
                            gradientAnimationDelay = 0,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                },
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                labelHelperProperties = LabelHelperProperties(
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ),
                popupProperties = PopupProperties(
                    textStyle = MaterialTheme.typography.labelSmall.copy(MaterialTheme.colorScheme.onSecondaryContainer),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentBuilder = { speed ->
                        onShowSpeedPoints(speed)
                        speed.format(1)
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = onCloseBottomSheet) {
                Text(text = stringResource(R.string.close))
            }
        }
    }
}

@Composable
private fun TrackRouteChooseFilterContent(
    state: TrackRouteChooseFilterState,
    onRadioButtonClick: (FilterOptions) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.filter_by),
                style = MaterialTheme.typography.titleMedium
            )
        }

        HorizontalDivider()

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            FilterTypeHeader(stringResource(R.string.date_created))

            DateFilterOptions.entries.forEachIndexed { index, option ->
                RadioButtonWithText(
                    index = index,
                    selected = state.selectedDateFilterOption == option,
                    option = when (option) {
                        DateFilterOptions.All -> stringResource(R.string.all)
                        DateFilterOptions.Today -> stringResource(R.string.today)
                        DateFilterOptions.Week -> stringResource(R.string.last_7_days)
                        DateFilterOptions.Month -> stringResource(R.string.last_30_days)
                        DateFilterOptions.Year -> stringResource(R.string.this_year)
                    },
                    onClick = { selectedOption ->
                        onRadioButtonClick(
                            FilterOptions.Date(
                                DateFilterOptions.entries[selectedOption]
                            )
                        )
                    }
                )
            }

            FilterTypeHeader(stringResource(R.string.distance))

            DistanceFilterOptions.entries.forEachIndexed { index, option ->
                RadioButtonWithText(
                    index = index,
                    selected = state.selectedDistanceFilterOption == option,
                    option = when (option) {
                        DistanceFilterOptions.All -> stringResource(R.string.all)
                        DistanceFilterOptions.Short -> stringResource(R.string.less_than_value_nm).format(
                            option.value
                        )

                        DistanceFilterOptions.Mid -> stringResource(R.string.less_than_value_nm).format(
                            option.value
                        )

                        DistanceFilterOptions.Long -> stringResource(R.string.less_than_value_nm).format(
                            option.value
                        )

                        DistanceFilterOptions.ExtraLong -> stringResource(R.string.more_than_value_nm).format(
                            option.value
                        )
                    },
                    onClick = { selectedOption ->
                        onRadioButtonClick(
                            FilterOptions.Distance(
                                DistanceFilterOptions.entries[selectedOption]
                            )
                        )
                    }
                )
            }

            FilterTypeHeader(stringResource(R.string.duration))

            DurationFilterOptions.entries.forEachIndexed { index, option ->
                RadioButtonWithText(
                    index = index,
                    selected = state.selectedDurationFilterOption == option,
                    option = when (option) {
                        DurationFilterOptions.All -> stringResource(R.string.all)
                        DurationFilterOptions.OneHour -> stringResource(R.string.less_than_1_hour)
                        DurationFilterOptions.SixHours -> stringResource(R.string.less_than_6_hours)
                        DurationFilterOptions.TwelveHours -> stringResource(R.string.less_than_12_hours)
                        DurationFilterOptions.Day -> stringResource(R.string.less_than_day)
                        DurationFilterOptions.MoreThanDay -> stringResource(R.string.more_than_day)
                    },
                    onClick = { selectedOption ->
                        onRadioButtonClick(
                            FilterOptions.Duration(
                                DurationFilterOptions.entries[selectedOption]
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterTypeHeader(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrackRouteChooseSorterContent(
    state: TrackRouteChooseSorterState,
    onRadioButtonClick: (SortOptions) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleMedium
            )
        }

        HorizontalDivider()

        SortOptions.entries.forEachIndexed { index, option ->
            RadioButtonWithText(
                index = index,
                selected = state.selectedOption == option,
                option = when (option) {
                    SortOptions.Name -> stringResource(R.string.name)
                    SortOptions.Recent -> stringResource(R.string.recent)
                    SortOptions.Distance -> stringResource(R.string.distance)
                    SortOptions.Duration -> stringResource(R.string.duration)
                },
                onClick = { selectedOption -> onRadioButtonClick(SortOptions.entries[selectedOption]) }
            )
        }
    }
}

@Composable
private fun RadioButtonWithText(
    index: Int,
    selected: Boolean,
    option: String,
    onClick: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(index)
            }
    ) {

        RadioButton(selected = selected, onClick = { onClick(index) })

        Text(
            text = option,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

    }
}

@Composable
private fun RecordItem(
    item: TrackedRouteItem,
    isEditModeEnabled: Boolean,
    offsetX: () -> Float,
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX().toInt(), 0) }
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
            Text(text = stringResource(R.string.duration) + ": ${formatDuration(item.duration)}")
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun TrackRouteContentPreview() {
    TrackRouteMainContent(
        state = TrackRouteMainContentState(),
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
        ),
        offsetX = { emptyMap() },
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
private fun TrackRouteChooseFilterContentPreview() {
    TrackRouteChooseFilterContent(
        state = TrackRouteChooseFilterState(),
        onRadioButtonClick = {},
        onNavigateBack = {}
    )
}

@Composable
@Preview(showBackground = true)
private fun TrackRouteChooseSorterContentPreview() {
    TrackRouteChooseSorterContent(
        state = TrackRouteChooseSorterState(),
        onRadioButtonClick = {},
        onNavigateBack = {}
    )
}

@Composable
@Preview(showBackground = true)
private fun RadioButtonWithTextPreview() {
    RadioButtonWithText(
        index = 0,
        selected = false,
        option = SortOptions.Recent.name,
        onClick = {}
    )
}
