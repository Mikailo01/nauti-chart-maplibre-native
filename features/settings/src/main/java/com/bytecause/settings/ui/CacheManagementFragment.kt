package com.bytecause.settings.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.data.services.HarboursUpdateService
import com.bytecause.features.settings.R
import com.bytecause.features.settings.databinding.CacheManagementLayoutBinding
import com.bytecause.presentation.components.compose.ConfirmationDialog
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.settings.ui.event.CacheManagementEffect
import com.bytecause.settings.ui.event.CacheManagementEvent
import com.bytecause.settings.ui.state.CacheManagementState
import com.bytecause.settings.ui.viewmodel.CacheManagementViewModel
import com.bytecause.util.delegates.viewBinding
import java.util.Date
import java.util.Locale

sealed interface ConfirmationDialogType {
    data object ClearVessels : ConfirmationDialogType
    data object ClearHarbours : ConfirmationDialogType
    data object ClearSearchHistory : ConfirmationDialogType
    data class ClearPoiRegion(val regionId: Int) : ConfirmationDialogType
}

sealed interface UpdateInterval {
    data class OneWeek(val interval: Long = 604_800_000L) : UpdateInterval
    data class TwoWeeks(val interval: Long = 1_209_600_000L) : UpdateInterval
    data class OneMonth(val interval: Long = 2_629_746_000L) : UpdateInterval
}

class CacheManagementFragment : Fragment(R.layout.cache_management_layout) {

    private val binding by viewBinding(CacheManagementLayoutBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cacheManagementComposeContent.setContent {
            AppTheme {
                CacheManagementScreen(
                    onNavigateBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun CacheManagementScreen(
    viewModel: CacheManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CacheManagementEffect.NavigateBack -> onNavigateBack()
                CacheManagementEffect.RegionUpdateFailure -> {
                    state.snackbarHostState.showSnackbar(
                        context.getString(com.bytecause.core.resources.R.string.network_error)
                    )
                }

                CacheManagementEffect.RegionUpdateSuccess -> {
                    state.snackbarHostState.showSnackbar(
                        context.getString(com.bytecause.core.resources.R.string.region_update_success)
                    )
                }

                CacheManagementEffect.HarboursUpdateFailure -> {
                    state.snackbarHostState.showSnackbar(
                        context.getString(com.bytecause.core.resources.R.string.network_error)
                    )
                }

                CacheManagementEffect.HarboursUpdateSuccess -> {
                    state.snackbarHostState.showSnackbar(
                        context.getString(com.bytecause.core.resources.R.string.harbours_update_success)
                    )
                }
            }
        }
    }

    CacheManagementContent(
        state = state,
        onEvent = viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheManagementContent(
    state: CacheManagementState,
    onEvent: (CacheManagementEvent) -> Unit
) {
    val activity = LocalContext.current as Activity

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = com.bytecause.core.resources.R.string.cache_management,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = { onEvent(CacheManagementEvent.OnNavigateBack) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) }
    ) { innerPadding ->
        Box {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Text(
                                text = stringResource(id = com.bytecause.core.resources.R.string.harbours),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = state.harboursModel.timestamp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        ChipsRow(
                            interval = state.harboursModel.harboursUpdateInterval,
                            onClick = { onEvent(CacheManagementEvent.OnSetHarboursUpdateInterval(it)) })

                        if (state.harboursModel.isUpdating) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(
                                        top = 20.dp,
                                        bottom = 20.dp
                                    )
                                    .fillMaxWidth()
                            )
                            if (state.harboursModel.progress != -1) {
                                Text(
                                    text = stringResource(id = com.bytecause.core.resources.R.string.processed_count)
                                        .format(state.harboursModel.progress),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (state.harboursModel.timestamp.isNotBlank()) {
                            ActionButtons(
                                isUpdating = state.harboursModel.isUpdating,
                                onForceUpdateClick = {

                                    Intent(activity, HarboursUpdateService::class.java).also {
                                        it.setAction(HarboursUpdateService.Actions.START.toString())
                                        activity.startService(it)
                                    }

                                    onEvent(CacheManagementEvent.OnUpdateHarbours)
                                },
                                onCancelUpdateClick = {
                                    onEvent(
                                        CacheManagementEvent.OnCancelHarboursUpdate
                                    )
                                },
                                onClearButtonClick = {
                                    onEvent(
                                        CacheManagementEvent.OnShowConfirmationDialog(
                                            ConfirmationDialogType.ClearHarbours
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        Row {
                            Text(
                                text = "POIs",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ChipsRow(
                            interval = state.poiUpdateInterval,
                            onClick = { onEvent(CacheManagementEvent.OnSetPoiUpdateInterval(it)) })

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        val locale = Locale.getDefault().language

                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(
                                state.downloadedRegions.values.toList(),
                                key = { it.regionId }) { item ->
                                val regionName = item.names["name:$locale"]
                                    ?: item.names["name:en"]
                                    ?: item.names["name"]

                                regionName?.let { name ->
                                    DownloadedRegionItem(
                                        regionId = item.regionId,
                                        regionName = name,
                                        timeStamp = item.timestamp,
                                        isUpdating = item.isUpdating,
                                        progress = item.progress,
                                        onClear = {
                                            onEvent(
                                                CacheManagementEvent.OnShowConfirmationDialog(
                                                    ConfirmationDialogType.ClearPoiRegion(it)
                                                )
                                            )
                                        },
                                        onUpdate = { regionId ->

                                            Intent(
                                                activity,
                                                HarboursUpdateService::class.java
                                            ).also {
                                                it.setAction(HarboursUpdateService.Actions.START.toString())
                                                it.putExtra("regionId", regionId)
                                                it.putExtra(
                                                    "regionName",
                                                    state.downloadedRegions[regionId]?.names?.get("name")
                                                )
                                                activity.startService(it)
                                            }

                                            onEvent(CacheManagementEvent.OnUpdateRegion(regionId))
                                        },
                                        onCancel = {
                                            onEvent(CacheManagementEvent.OnCancelRegionUpdate(it))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        Row {
                            Text(
                                text = stringResource(id = com.bytecause.core.resources.R.string.vessels),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = state.vesselsTimestamp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    onEvent(
                                        CacheManagementEvent.OnShowConfirmationDialog(
                                            ConfirmationDialogType.ClearVessels
                                        )
                                    )
                                },
                                modifier = Modifier.wrapContentHeight()
                            ) {
                                Text(
                                    text = stringResource(id = com.bytecause.core.resources.R.string.clear),
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = com.bytecause.core.resources.R.string.search_history),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = {
                                onEvent(
                                    CacheManagementEvent.OnShowConfirmationDialog(
                                        ConfirmationDialogType.ClearSearchHistory
                                    )
                                )
                            },
                            modifier = Modifier.wrapContentHeight()
                        ) {
                            Text(
                                text = stringResource(id = com.bytecause.core.resources.R.string.clear),
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }

        if (state.showConfirmationDialog != null) {
            ConfirmationDialog(
                onDismiss = {
                    onEvent(
                        CacheManagementEvent.OnShowConfirmationDialog(
                            null
                        )
                    )
                },
                onConfirm = {
                    when (state.showConfirmationDialog) {
                        ConfirmationDialogType.ClearVessels -> {
                            onEvent(CacheManagementEvent.OnClearVessels)
                        }

                        ConfirmationDialogType.ClearHarbours -> {
                            onEvent(CacheManagementEvent.OnClearHarbours)
                        }

                        ConfirmationDialogType.ClearSearchHistory -> {
                            onEvent(CacheManagementEvent.OnClearSearchHistory)
                        }

                        is ConfirmationDialogType.ClearPoiRegion -> {
                            onEvent(
                                CacheManagementEvent.OnDeleteRegion(
                                    state.showConfirmationDialog.regionId
                                )
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(id = com.bytecause.core.resources.R.string.data_will_be_permanently_deleted_),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ChipsRow(interval: UpdateInterval?, onClick: (UpdateInterval) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = com.bytecause.core.resources.R.string.update_interval),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = interval == UpdateInterval.OneWeek(),
                onClick = { onClick(UpdateInterval.OneWeek()) },
                label = { Text(text = stringResource(id = com.bytecause.core.resources.R.string.one_week)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                        contentDescription = null
                    )
                },
                border = BorderStroke(
                    2.dp,
                    colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                ),
                colors = FilterChipDefaults.filterChipColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.inversePrimary
                )
            )

            FilterChip(
                selected = interval == UpdateInterval.TwoWeeks(),
                onClick = { onClick(UpdateInterval.TwoWeeks()) },
                label = { Text(text = stringResource(id = com.bytecause.core.resources.R.string.two_weeks)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                        contentDescription = null
                    )
                },
                border = BorderStroke(
                    2.dp,
                    colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                ),
                colors = FilterChipDefaults.filterChipColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.inversePrimary
                )
            )

            FilterChip(
                selected = interval == UpdateInterval.OneMonth(),
                onClick = { onClick(UpdateInterval.OneMonth()) },
                label = { Text(text = stringResource(id = com.bytecause.core.resources.R.string.one_month)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                        contentDescription = null
                    )
                },
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
    }
}

@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    isUpdating: Boolean,
    onForceUpdateClick: () -> Unit,
    onCancelUpdateClick: () -> Unit,
    onClearButtonClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(
            onClick = {
                if (isUpdating) onCancelUpdateClick()
                else onForceUpdateClick()
            },
            modifier = Modifier.wrapContentHeight()
        ) {
            if (isUpdating) Text(
                text = stringResource(id = com.bytecause.core.resources.R.string.cancel),
                fontSize = 14.sp
            )
            else Text(
                text = stringResource(id = com.bytecause.core.resources.R.string.force_update),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = modifier.width(20.dp))

        OutlinedButton(
            onClick = onClearButtonClick,
            modifier = Modifier.wrapContentHeight()
        ) {
            Text(
                text = stringResource(id = com.bytecause.core.resources.R.string.clear),
                fontSize = 14.sp,
                color = Color.Red
            )
        }
    }
}


@Composable
fun DownloadedRegionItem(
    regionId: Int,
    regionName: String,
    timeStamp: Long,
    isUpdating: Boolean,
    progress: Int = -1,
    onClear: (Int) -> Unit,
    onUpdate: (Int) -> Unit,
    onCancel: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Icon(
                painter = painterResource(id = com.bytecause.core.resources.R.drawable.earth_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(text = regionName, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.weight(1f))

            val date = Date(timeStamp)
            val formattedDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", date).toString()

            Text(text = formattedDate, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }

        if (isUpdating) {
            LinearProgressIndicator()
            if (progress != -1) {
                Text(
                    text = stringResource(id = com.bytecause.core.resources.R.string.processed_count)
                        .format(progress),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ActionButtons(
            isUpdating = isUpdating,
            onForceUpdateClick = { onUpdate(regionId) },
            onCancelUpdateClick = { onCancel(regionId) },
            onClearButtonClick = { onClear(regionId) }
        )
    }
}

@Composable
@Preview
fun CacheManagementContentPreview() {
    CacheManagementContent(
        state = CacheManagementState(),
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
fun DownloadedRegionItemPreview() {
    DownloadedRegionItem(
        regionId = 0,
        regionName = "Jihomoravský kraj",
        timeStamp = 1724861327000L,
        isUpdating = true,
        progress = 10000,
        onClear = {},
        onUpdate = {},
        onCancel = {}
    )
}