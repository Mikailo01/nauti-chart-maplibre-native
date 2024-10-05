package com.bytecause.map.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.AnchorageAlarmSettingsDialogLayoutBinding
import com.bytecause.map.ui.effect.AnchorageAlarmSettingsEffect
import com.bytecause.map.ui.event.AnchorageAlarmSettingsEvent
import com.bytecause.map.ui.event.IntervalType
import com.bytecause.map.ui.state.AnchorageAlarmSettingsState
import com.bytecause.map.ui.viewmodel.AnchorageAlarmSettingsViewModel
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AnchorageAlarmSettingsComposedDialog :
    DialogFragment(R.layout.anchorage_alarm_settings_dialog_layout) {

    private val binding by viewBinding(AnchorageAlarmSettingsDialogLayoutBinding::bind)

    private val viewModel by viewModels<AnchorageAlarmSettingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.anchorageAlarmSettingsComposeView.setContent {
            AppTheme {
                AnchorageSettingsScreen(
                    viewModel,
                    onNavigateBack = { findNavController().popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorageSettingsScreen(
    viewModel: AnchorageAlarmSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AnchorageAlarmSettingsEffect.NavigateBack -> onNavigateBack()
                AnchorageAlarmSettingsEffect.OnShowIntervalBottomSheet -> {
                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    } else bottomSheetScaffoldState.bottomSheetState.show()
                        .also { Log.d("idk", "showed") }
                }
            }
        }
    }

    AnchorageSettingsScreenContent(
        state = state,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        onEvent = viewModel::uiEventHandler
    )
}

// TODO("Fix recompositions")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorageSettingsScreenContent(
    state: AnchorageAlarmSettingsState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onEvent: (AnchorageAlarmSettingsEvent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = com.bytecause.core.resources.R.string.anchorage_alarm_settings,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
                onNavigationIconClick = { onEvent(AnchorageAlarmSettingsEvent.OnNavigateBack) }
            )
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
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Column {
                    Text(
                        "GPS",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        GpsRowItem(
                            textRes = com.bytecause.core.resources.R.string.maximum_update_interval,
                            interval = state.maxGpsUpdateInterval,
                            onItemClick = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                                        bottomSheetScaffoldState.bottomSheetState.hide()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                        // save interval type to be able to infer which property should be updated
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnUpdateIntervalType(
                                                IntervalType.MAX_UPDATE_INTERVAL
                                            )
                                        )
                                    }
                                }
                            }
                        )

                        GpsRowItem(
                            textRes = com.bytecause.core.resources.R.string.minimum_update_interval,
                            interval = state.minGpsUpdateInterval,
                            onItemClick = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                                        bottomSheetScaffoldState.bottomSheetState.hide()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                        // save interval type to be able to infer which property should be updated
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnUpdateIntervalType(
                                                IntervalType.MIN_UPDATE_INTERVAL
                                            )
                                        )
                                    }
                                }
                            }
                        )

                        GpsRowItem(
                            textRes = com.bytecause.core.resources.R.string.alarm_delay,
                            interval = state.alarmDelay,
                            onItemClick = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                                        bottomSheetScaffoldState.bottomSheetState.hide()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                        // save interval type to be able to infer which property should be updated
                                        onEvent(
                                            AnchorageAlarmSettingsEvent.OnUpdateIntervalType(
                                                IntervalType.ALARM_DELAY
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(com.bytecause.core.resources.R.string.anchorage_locations),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = stringResource(com.bytecause.core.resources.R.string.show_anchorages),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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

                Column {
                    Text(
                        text = stringResource(com.bytecause.core.resources.R.string.anchorage_history),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Lazy column
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
                            state.selectedIntervalType?.let { intervalType ->
                                onEvent(
                                    AnchorageAlarmSettingsEvent.OnSelectedIntervalValueChange(
                                        intervalType,
                                        value
                                    )
                                )
                            }
                        })
                    }
                },
                scaffoldState = bottomSheetScaffoldState
            ) {}
        }
    }
}

@Composable
fun GpsRowItem(
    @StringRes textRes: Int,
    interval: Int,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .clickable { onItemClick() }) {
        Text(
            stringResource(textRes),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            stringResource(com.bytecause.core.resources.R.string.placeholder_with_seconds)
                .format(interval),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    minValue: Int = 1,
    maxValue: Int = 15,
    onValueSelected: (Int) -> Unit
) {
    val numbers = remember {
        (minValue..maxValue).toList()
    }
    val listState = rememberLazyListState()
    var selectedInt by remember { mutableIntStateOf(minValue) }

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
                modifier = Modifier.height(150.dp),
                state = listState,
                verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(vertical = 60.dp) // Add padding to center items
            ) {
                items(numbers) { number ->
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