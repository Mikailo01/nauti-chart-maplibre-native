package com.bytecause.map.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.AnchorageAlarmSettingsDialogLayoutBinding
import com.bytecause.map.ui.effect.AnchorageAlarmSettingsEffect
import com.bytecause.map.ui.event.AnchorageAlarmSettingsEvent
import com.bytecause.map.ui.model.AnchorageHistoryDeletionInterval
import com.bytecause.map.ui.model.AnchorageHistoryUiModel
import com.bytecause.map.ui.model.BottomSheetType
import com.bytecause.map.ui.state.AnchorageAlarmSettingsState
import com.bytecause.map.ui.viewmodel.AnchorageAlarmSettingsViewModel
import com.bytecause.map.util.MapUtil
import com.bytecause.presentation.components.compose.ConfirmationDialog
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.common.getDateTimeFromTimestamp
import com.bytecause.util.compose.swipeToDismiss
import com.bytecause.util.compose.then
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AnchorageAlarmSettingsComposedDialog :
    DialogFragment(R.layout.anchorage_alarm_settings_dialog_layout) {

    private val binding by viewBinding(AnchorageAlarmSettingsDialogLayoutBinding::bind)

    private val viewModel by viewModels<AnchorageAlarmSettingsViewModel>()

    private val mapSharedViewModel by activityViewModels<MapSharedViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.anchorageAlarmSettingsComposeView.setContent {
            AppTheme {
                AnchorageSettingsScreen(
                    viewModel = viewModel,
                    mapSharedViewModel = mapSharedViewModel,
                    onNavigateBack = { findNavController().popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnchorageSettingsScreen(
    viewModel: AnchorageAlarmSettingsViewModel,
    mapSharedViewModel: MapSharedViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val context = LocalContext.current

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = remember {
            SheetState(
                density = density,
                skipPartiallyExpanded = false,
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
        }
    )

    BackHandler(
        enabled = bottomSheetScaffoldState.bottomSheetState.isVisible
    ) {
        viewModel.uiEventHandler(AnchorageAlarmSettingsEvent.OnShowBottomSheet(null))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AnchorageAlarmSettingsEffect.NavigateBack -> onNavigateBack()
                is AnchorageAlarmSettingsEffect.AnchorageHistoryItemClick -> {
                    mapSharedViewModel.setAnchorageLocationFromHistoryId(effect.id)
                    onNavigateBack()
                }

                is AnchorageAlarmSettingsEffect.AnchorageHistoryDeletionIntervalClick -> {
                    state.snackbarHostState.showSnackbar(
                        if (effect.intervalType == AnchorageHistoryDeletionInterval.INFINITE) {
                            context.getString(
                                com.bytecause.core.resources.R.string.anchorage_history_will_not_be_deleted_automatically
                            )
                        } else {
                            context.getString(
                                com.bytecause.core.resources.R.string.anchorage_history_will_be_deleted_automatically_after_value_days
                            )
                                .format(effect.intervalType.displayText)
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(state.bottomSheetType) {
        if (state.bottomSheetType == null) {
            bottomSheetScaffoldState.bottomSheetState.hide()
        } else bottomSheetScaffoldState.bottomSheetState.expand()
    }

    AnchorageSettingsScreenContent(
        state = state,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        onEvent = viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnchorageSettingsScreenContent(
    state: AnchorageAlarmSettingsState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onEvent: (AnchorageAlarmSettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = com.bytecause.core.resources.R.string.anchorage_alarm_settings,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
                onNavigationIconClick = { onEvent(AnchorageAlarmSettingsEvent.OnNavigateBack) }
            )
        },
        snackbarHost = {
            SnackbarHost(state.snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                ) {
                    Column {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "GPS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            HorizontalDivider(
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Column {
                            GpsRowItem(
                                textRes = com.bytecause.core.resources.R.string.maximum_update_interval,
                                interval = state.maxGpsUpdateInterval,
                                onItemClick = {
                                    onEvent(
                                        AnchorageAlarmSettingsEvent.OnShowBottomSheet(
                                            BottomSheetType.MAX_UPDATE_INTERVAL
                                        )
                                    )
                                }
                            )

                            GpsRowItem(
                                textRes = com.bytecause.core.resources.R.string.minimum_update_interval,
                                interval = state.minGpsUpdateInterval,
                                onItemClick = {
                                    onEvent(
                                        AnchorageAlarmSettingsEvent.OnShowBottomSheet(
                                            BottomSheetType.MIN_UPDATE_INTERVAL
                                        )
                                    )
                                }
                            )

                            GpsRowItem(
                                textRes = com.bytecause.core.resources.R.string.alarm_delay,
                                interval = state.alarmDelay,
                                onItemClick = {
                                    onEvent(
                                        AnchorageAlarmSettingsEvent.OnShowBottomSheet(
                                            BottomSheetType.ALARM_DELAY
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(com.bytecause.core.resources.R.string.anchorage_locations),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = stringResource(com.bytecause.core.resources.R.string.show_anchorages),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = state.areAnchorageLocationsVisible,
                                    onCheckedChange = { boolean ->
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnAnchorageVisibilityChange(
                                                boolean
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(com.bytecause.core.resources.R.string.tracking),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(com.bytecause.core.resources.R.string.track_movement),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stringResource(com.bytecause.core.resources.R.string.track_movement_hint),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                Switch(
                                    checked = state.trackMovement,
                                    onCheckedChange = { boolean ->
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnTrackMovementStateChange(
                                                boolean
                                            )
                                        )
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(com.bytecause.core.resources.R.string.track_battery_state),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = stringResource(com.bytecause.core.resources.R.string.track_battery_state_hint),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                Switch(
                                    checked = state.trackBatteryState,
                                    onCheckedChange = { boolean ->
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnTrackBatteryStateChange(
                                                boolean
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(com.bytecause.core.resources.R.string.anchorage_history),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnAnchorageHistoryDeletionIntervalClick
                                        )
                                    }
                            ) {
                                Icon(
                                    painter = painterResource(com.bytecause.core.resources.R.drawable.interval),
                                    contentDescription = if (state.anchorageHistoryDeletionInterval == AnchorageHistoryDeletionInterval.INFINITE) {
                                        stringResource(com.bytecause.core.resources.R.string.anchorage_history_will_not_be_deleted_automatically)
                                    } else {
                                        stringResource(com.bytecause.core.resources.R.string.anchorage_history_will_be_deleted_automatically_after_value_days)
                                            .format(state.anchorageHistoryDeletionInterval.displayText)
                                    },
                                    modifier = Modifier.padding(5.dp)
                                )
                                Text(
                                    text = state.anchorageHistoryDeletionInterval.displayText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.then(
                                        state.anchorageHistoryDeletionInterval == AnchorageHistoryDeletionInterval.INFINITE,
                                        onTrue = { padding(bottom = 2.dp) })
                                )
                            }

                            if (state.anchorageHistory.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onEvent(AnchorageAlarmSettingsEvent.OnToggleEditMode)
                                    },
                                    colors = if (state.isEditMode) {
                                        IconButtonDefaults.iconButtonColors()
                                            .copy(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                    } else IconButtonDefaults.iconButtonColors()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(com.bytecause.core.resources.R.string.edit)
                                    )
                                }

                                IconButton(onClick = {
                                    onEvent(
                                        AnchorageAlarmSettingsEvent.OnClearHistoryConfirmDialogStateChange(
                                            true
                                        )
                                    )
                                }) {
                                    Icon(
                                        painter = painterResource(com.bytecause.core.resources.R.drawable.baseline_clear_all_24),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (state.anchorageHistory.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(com.bytecause.core.resources.R.string.empty_history),
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 0.dp, max = 200.dp)
                            ) {
                                itemsIndexed(
                                    state.anchorageHistory,
                                    key = { _, item -> item.id }) { index, item ->
                                    AnchorageHistoryItem(
                                        item = item,
                                        index = index,
                                        isEditEnabled = state.isEditMode,
                                        onItemClick = {
                                            onEvent(
                                                AnchorageAlarmSettingsEvent.OnAnchorageHistoryItemClick(
                                                    it
                                                )
                                            )
                                        },
                                        onRemove = {
                                            onEvent(
                                                AnchorageAlarmSettingsEvent.OnRemoveAnchorageHistoryItem(
                                                    it
                                                )
                                            )
                                        }
                                    )
                                    if (item != state.anchorageHistory.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BottomSheetScaffold(
                modifier = Modifier.align(Alignment.BottomCenter),
                sheetContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        NumberPicker(onValueSelected = { value: Int ->
                            onEvent(
                                AnchorageAlarmSettingsEvent.OnSelectedIntervalValueChange(value)
                            )
                        })
                    }
                },
                scaffoldState = bottomSheetScaffoldState,
                sheetSwipeEnabled = false
            ) {}

            if (state.showDeleteHistoryConfirmationDialog) {
                ConfirmationDialog(
                    modifier = Modifier.align(Alignment.Center),
                    onDismiss = {
                        onEvent(
                            AnchorageAlarmSettingsEvent.OnClearHistoryConfirmDialogStateChange(
                                false
                            )
                        )
                    }, onConfirm = {
                        onEvent(AnchorageAlarmSettingsEvent.OnDeleteAnchorageHistory)
                    }
                ) {
                    Text(
                        text = "Entire anchorage history list will be cleared.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun GpsRowItem(
    @StringRes textRes: Int,
    interval: Int,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .clickable { onItemClick() }) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp)
        )
        Text(
            stringResource(com.bytecause.core.resources.R.string.placeholder_with_seconds)
                .format(interval),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
        )
    }
}

@Composable
private fun NumberPicker(
    modifier: Modifier = Modifier,
    minValue: Int = 1,
    maxValue: Int = 15,
    onValueSelected: (Int) -> Unit
) {
    val numbers = remember {
        (minValue..maxValue).toList()
    }
    val listState = rememberLazyListState()
    var selectedInt by rememberSaveable { mutableIntStateOf(minValue) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Box(
            modifier = modifier
                .height(150.dp)
                .fillMaxWidth()
        ) {
            var isScrolling by remember { mutableStateOf(false) }

            // Listen to the scroll state and detect when the user finishes scrolling
            LaunchedEffect(listState.isScrollInProgress) {
                if (!listState.isScrollInProgress && isScrolling) {
                    // Determine which item is closest to the center
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isNotEmpty()) {
                        selectedInt =
                            numbers.firstOrNull { it == listState.firstVisibleItemIndex + 1 }
                                ?: minValue
                    }

                    isScrolling = false
                } else if (listState.isScrollInProgress) {
                    isScrolling = true
                }
            }

            LaunchedEffect(isScrolling) {
                if (!isScrolling) {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex)
                }
            }

            // Number picker LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(vertical = 60.dp) // Add padding to center items
            ) {
                itemsIndexed(numbers) { index, number ->
                    // Animation based on selection
                    val alpha by animateFloatAsState(if (selectedInt == number) 1f else 0.3f)
                    val scale by animateFloatAsState(if (selectedInt == number) 1.2f else 1f)

                    // Draw each item
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha = alpha,
                                scaleX = scale,
                                scaleY = scale
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // Visual indicator for the center selection area
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(50.dp) // Size of the central selection area
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.2f)) // Optional: semi-transparent background
            )
        }

        ElevatedButton(
            onClick = {
                onValueSelected(selectedInt)
            },
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                stringResource(com.bytecause.core.resources.R.string.done),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AnchorageHistoryItem(
    item: AnchorageHistoryUiModel,
    isEditEnabled: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    // Animatable for horizontal offset
    val offsetX = remember {
        Animatable(0f)
    }

    var hasFinished by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(isEditEnabled) {
        if (isEditEnabled && hasFinished.not()) {
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
            .clickable {
                onItemClick(item.id)
            }
            .offset { IntOffset(offsetX.value.toInt(), 0) }
            .then(isEditEnabled, onTrue = { swipeToDismiss { onRemove(item.id) } })
    ) {
        Text(text = getDateTimeFromTimestamp(item.timestamp))
        Text(
            text = stringResource(
                com.bytecause.core.resources.R.string.split_two_strings_formatter,
                MapUtil.latitudeToDMS(item.latitude),
                MapUtil.longitudeToDMS(item.longitude)
            )
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = stringResource(com.bytecause.core.resources.R.string.radius))
            Text(
                text = stringResource(com.bytecause.core.resources.R.string.value_with_unit_placeholder).format(
                    item.radius,
                    "M"
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun AnchorageSettingsScreenContentPreview() {
    AnchorageSettingsScreenContent(
        state = AnchorageAlarmSettingsState(),
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        onEvent = {}
    )
}