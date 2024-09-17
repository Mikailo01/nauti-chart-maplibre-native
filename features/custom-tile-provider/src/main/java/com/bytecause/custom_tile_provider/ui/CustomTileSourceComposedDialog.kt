package com.bytecause.custom_tile_provider.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.custom_tile_provider.ui.events.CustomTileSourceEffect
import com.bytecause.custom_tile_provider.ui.events.CustomTileSourceEvent
import com.bytecause.custom_tile_provider.ui.state.CustomTileSourceState
import com.bytecause.custom_tile_provider.ui.state.TileNameError
import com.bytecause.custom_tile_provider.ui.viewmodel.CustomTileSourceDialogViewModel
import com.bytecause.custom_tile_provider.util.AnalyzeCustomOnlineTileProvider.extractTileUrlAttrs
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.nautichart.features.custom_tile_provider.databinding.CustomTileSourceComposedDialogBinding
import com.bytecause.presentation.components.compose.CustomOutlinedButton
import com.bytecause.presentation.components.compose.CustomRangeSlider
import com.bytecause.presentation.components.compose.Divider
import com.bytecause.presentation.components.compose.TileSizeChips
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.components.compose.ZoomLevels
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.file.FileUtil.checkFilenameExists
import com.bytecause.util.file.FileUtil.copyFileToFolder
import com.bytecause.util.file.FileUtil.offlineTilesDir
import com.bytecause.util.file.FileUtil.queryName
import com.bytecause.util.map.MbTileType
import com.bytecause.util.map.MbTilesLoader
import com.spr.jetpack_loading.components.indicators.BallPulseRiseIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 0f
private const val MAX_ZOOM = 24f

@AndroidEntryPoint
class CustomTileSourceComposedDialog : Fragment(com.bytecause.nautichart.features.custom_tile_provider.R.layout.custom_tile_source_composed_dialog) {

    private val binding by viewBinding(
        CustomTileSourceComposedDialogBinding::bind
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.customTileSourceDialog.setContent {
            AppTheme {
                CustomTileSourceComposedDialogScreen(
                    onNavigateBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTileSourceComposedDialogScreen(
    viewModel: CustomTileSourceDialogViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    var fileUri by remember {
        mutableStateOf<String?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        if (queryName(context.contentResolver, uri)?.endsWith(".mbtiles") == false) {
            coroutineScope.launch {
                state.snackbarHostState.showSnackbar(context.getString(R.string.invalid_file_type))
            }
        } else {
            fileUri = uri.toString()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomTileSourceEffect.TabClick -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(effect.value)
                    }
                }

                CustomTileSourceEffect.DoneButtonClick -> {
                    when (pagerState.currentPage) {
                        0 -> {
                            if (state.sourceName.isBlank() || state.urlValue.isBlank() || state.tileSize == -1) {

                                if (state.sourceName.isBlank()) {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnSourceNameError(
                                            TileNameError.Empty
                                        )
                                    )
                                }

                                if (state.urlValue.isBlank()) {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnUrlValidationChange(
                                            false
                                        )
                                    )
                                }

                                if (state.tileSize == -1) {
                                    viewModel.uiEventHandler(CustomTileSourceEvent.OnTileSizeNotSelected)
                                }

                                return@collect
                            }

                            extractTileUrlAttrs(
                                // Url shouldn't contain any whitespaces
                                state.urlValue.takeIf { !it.contains(" ") } ?: run {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnUrlValueChange(
                                            state.urlValue.trimIndent()
                                        )
                                    )
                                    state.urlValue
                                }
                            )?.let { tileUrlInfo ->
                                // TODO("Add support for online vector tile provider.")
                                coroutineScope.launch {
                                    viewModel.saveOnlineRasterTileProvider(
                                        CustomTileProvider(
                                            CustomTileProviderType.Raster.Online(
                                                name = state.sourceName,
                                                url = tileUrlInfo.url,
                                                tileFileFormat = tileUrlInfo.tileFileFormat,
                                                minZoom = rangeSliderState.activeRangeStart.toInt(),
                                                maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                                tileSize = state.tileSize,
                                                imageUrl = formatTileUrl(tileUrlInfo.url)
                                            )
                                        )
                                    )

                                    onNavigateBack()
                                }
                            } ?: run {
                                viewModel.uiEventHandler(
                                    CustomTileSourceEvent.OnUrlValidationChange(
                                        false
                                    )
                                )
                            }
                        }

                        1 -> {
                            if (state.tileSize == -1 || state.sourceName.isBlank()) {
                                if (state.sourceName.isBlank()) {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnSourceNameError(TileNameError.Empty)
                                    )
                                }

                                if (state.tileSize == -1) {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnTileSizeNotSelected
                                    )
                                }

                                return@collect
                            }

                            coroutineScope.launch {
                                if (checkFilenameExists(
                                        state.sourceName,
                                        context.offlineTilesDir()
                                    )
                                ) {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnSourceNameError(
                                            TileNameError.Exists
                                        )
                                    )
                                } else {
                                    viewModel.uiEventHandler(
                                        CustomTileSourceEvent.OnLoadingValueChange(
                                            true
                                        )
                                    )

                                    fileUri?.let { uriString ->
                                        copyFileToFolder(
                                            contentResolver = context.contentResolver,
                                            fileUri = Uri.parse(uriString),
                                            destinationFolder = context.offlineTilesDir(),
                                            fileName = state.sourceName
                                        ).let {
                                            viewModel.uiEventHandler(
                                                CustomTileSourceEvent.OnLoadingValueChange(
                                                    true
                                                )
                                            )

                                            if (it != null) {
                                                val format = MbTilesLoader.getFormat(it)
                                                val minMaxZoom = MbTilesLoader.getMinMaxZoom(it)

                                                when (format) {
                                                    MbTileType.Vector -> {
                                                        viewModel.saveOfflineVectorTileProvider(
                                                            CustomTileProvider(
                                                                CustomTileProviderType.Vector.Offline(
                                                                    name = state.sourceName,
                                                                    minZoom = minMaxZoom.first,
                                                                    maxZoom = minMaxZoom.second,
                                                                    filePath = it.absolutePath
                                                                )
                                                            )
                                                        )
                                                    }

                                                    MbTileType.Raster -> {
                                                        viewModel.saveOfflineRasterTileProvider(
                                                            CustomTileProvider(
                                                                CustomTileProviderType.Raster.Offline(
                                                                    name = state.sourceName,
                                                                    minZoom = minMaxZoom.first,
                                                                    maxZoom = minMaxZoom.second,
                                                                    tileSize = state.tileSize,
                                                                    filePath = it.absolutePath
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            onNavigateBack()
                                        }
                                    } ?: run {
                                        // TODO("Handle uri null")
                                    }
                                }
                            }
                        }
                    }
                }

                CustomTileSourceEffect.LaunchFileManager -> launcher.launch("application/octet-stream")
                CustomTileSourceEffect.NavigateBack -> onNavigateBack()
                CustomTileSourceEffect.ToggleRangeSliderVisibility -> {
                    coroutineScope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        } else bottomSheetScaffoldState.bottomSheetState.show()
                    }
                }

                CustomTileSourceEffect.TileSizeNotSelected -> state.snackbarHostState.showSnackbar("Tile size not selected.")
            }
        }
    }

    CustomTileSourceComposedDialogContent(
        state = state,
        pagerState = pagerState,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        rangeSliderState = rangeSliderState,
        onEvent = viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomTileSourceComposedDialogContent(
    state: CustomTileSourceState,
    pagerState: PagerState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    rangeSliderState: RangeSliderState,
    modifier: Modifier = Modifier,
    onEvent: (CustomTileSourceEvent) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            titleRes = R.string.custom_tile_source,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationIconClick = { onEvent(CustomTileSourceEvent.OnNavigateBack) }
        )
    }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            androidx.compose.material3.TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    text = {
                        Text(text = stringResource(id = R.string.online))
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.online_storage),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onEvent(CustomTileSourceEvent.OnTabClick(0))
                    }
                )

                Tab(
                    selected = pagerState.currentPage == 1,
                    text = {
                        Text(text = stringResource(id = R.string.local))
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_storage_24),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onEvent(CustomTileSourceEvent.OnTabClick(1))
                    }
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxWidth(),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                        0 -> {
                            OnlineTileSourceContent(
                                state = state,
                                minZoom = rangeSliderState.activeRangeStart.toInt(),
                                maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                rangeSliderState = rangeSliderState,
                                bottomSheetScaffoldState = bottomSheetScaffoldState,
                                onEvent = onEvent
                            )
                        }

                        1 -> {
                            OfflineTileSourceContent(
                                state = state,
                                onEvent = onEvent
                            )
                        }
                    }

                    SnackbarHost(
                        hostState = state.snackbarHostState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )

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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineTileSourceContent(
    state: CustomTileSourceState,
    minZoom: Int,
    maxZoom: Int,
    rangeSliderState: RangeSliderState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onEvent: (CustomTileSourceEvent) -> Unit
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
                onValueChange = { onEvent(CustomTileSourceEvent.OnSourceNameChange(it)) },
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
                onValueChange = { onEvent(CustomTileSourceEvent.OnUrlValueChange(it)) },
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
                                Text(text = stringResource(id = R.string.url_is_not_in_valid_format))
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
                    onEvent(CustomTileSourceEvent.OnToggleRangeSliderVisibility)
                }
            )

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            TileSizeChips(tileSize = state.tileSize) {
                onEvent(CustomTileSourceEvent.OnTileSizeValueChange(it))
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
                    onClick = { onEvent(CustomTileSourceEvent.OnDoneButtonClick) }
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

@Composable
fun OfflineTileSourceContent(
    state: CustomTileSourceState,
    onEvent: (CustomTileSourceEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            value = state.sourceName,
            onValueChange = { onEvent(CustomTileSourceEvent.OnSourceNameChange(it)) },
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

        Divider(
            thickness = 1,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )

        TileSizeChips(tileSize = state.tileSize) {
            onEvent(CustomTileSourceEvent.OnTileSizeValueChange(it))
        }

        Divider(
            thickness = 1,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )

        Button(
            onClick = { onEvent(CustomTileSourceEvent.OnLaunchFileManager) },
            enabled = state.tileSize != -1
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
                onClick = { onEvent(CustomTileSourceEvent.OnDoneButtonClick) }
            )
        }
    }
}

private fun formatTileUrl(url: String, z: Int = 4, y: Int = 5, x: Int = 8): String {
    return url
        .replace("{z}", z.toString())
        .replace("{y}", y.toString())
        .replace("{x}", x.toString())
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun CustomTileSourceComposedDialogContentPreview() {
    CustomTileSourceComposedDialogContent(
        state = CustomTileSourceState(),
        pagerState = rememberPagerState(pageCount = { 2 }),
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        rangeSliderState = remember {
            RangeSliderState()
        },
        onEvent = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun OnlineTileSourceContentPreview() {
    OnlineTileSourceContent(
        state = CustomTileSourceState(),
        minZoom = 0,
        maxZoom = 24,
        rangeSliderState = remember {
            RangeSliderState()
        },
        onEvent = {},
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    )
}

@Composable
@Preview(showBackground = true)
fun OfflineTileSourceContentPreview() {
    OfflineTileSourceContent(
        state = CustomTileSourceState(),
        onEvent = {}
    )
}

