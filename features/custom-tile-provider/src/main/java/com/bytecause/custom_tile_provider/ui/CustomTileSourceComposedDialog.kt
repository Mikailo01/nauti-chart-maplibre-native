package com.bytecause.custom_tile_provider.ui

import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.custom_tile_provider.ui.viewmodel.CustomTileSourceDialogViewModel
import com.bytecause.custom_tile_provider.util.AnalyzeCustomOnlineTileProvider.extractTileUrlAttrs
import com.bytecause.domain.model.CustomTileProvider
import com.bytecause.domain.model.CustomTileProviderType
import com.bytecause.nautichart.features.custom_tile_provider.databinding.CustomTileSourceComposedDialogBinding
import com.bytecause.presentation.components.compose.CustomOutlinedButton
import com.bytecause.presentation.components.compose.CustomRangeSlider
import com.bytecause.presentation.components.compose.CustomTileSourceTextFields
import com.bytecause.presentation.components.compose.Divider
import com.bytecause.presentation.components.compose.IndeterminateCircularIndicator
import com.bytecause.presentation.components.compose.TileSizeChips
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.components.compose.ZoomLevels
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.file.FileUtil.copyFileToFolder
import com.bytecause.util.file.FileUtil.queryName
import com.bytecause.util.map.MbTileType
import com.bytecause.util.map.MbTilesLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


private const val MIN_ZOOM = 0f
private const val MAX_ZOOM = 26f

@AndroidEntryPoint
class CustomTileSourceComposedDialog : DialogFragment() {

    private val binding by viewBinding(
        CustomTileSourceComposedDialogBinding::inflate
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.customTileSourceDialog.setContent {
            MaterialTheme {
                CustomTileSourceComposedDialogScreen(
                    onNavigateBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Apply the fullscreen dialog style
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dialog_background
                )
            )
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CustomTileSourceComposedDialogScreen(
    viewModel: CustomTileSourceDialogViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    var sourceName by rememberSaveable {
        mutableStateOf("")
    }
    var urlValue by rememberSaveable {
        mutableStateOf("")
    }
    var tileSize by rememberSaveable {
        mutableIntStateOf(-1)
    }

    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    var isUrlValid by rememberSaveable {
        mutableStateOf(true)
    }

    val context = LocalContext.current
    val activity = (LocalContext.current as ContextWrapper).baseContext
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
                snackBarHostState.showSnackbar("Invalid file type.")
            }
        } else {
            fileUri = uri.toString()
        }
    }

    CustomTileSourceComposedDialogContent(
        sourceName = sourceName,
        urlValue = urlValue,
        tileSize = tileSize,
        pagerState = pagerState,
        isLoading = isLoading,
        snackBarHostState = snackBarHostState,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        rangeSliderState = rangeSliderState,
        isUrlValid = isUrlValid,
        onTabClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(it)
            }
        },
        onSourceNameValueChange = { sourceName = it.trimIndent() },
        onUrlValueChange = { urlValue = it.trimIndent() },
        onToggleRangeSliderVisibility = {
            coroutineScope.launch {
                if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                } else bottomSheetScaffoldState.bottomSheetState.show()
            }
        },
        onTileSizeValueChange = { tileSize = it },
        onLaunchFileManager = {
            launcher.launch("application/octet-stream")
        },
        onDoneButtonClick = {
            when (pagerState.currentPage) {
                0 -> {
                    if (sourceName.isBlank() || urlValue.isBlank() || tileSize == -1) return@CustomTileSourceComposedDialogContent

                    extractTileUrlAttrs(
                        // Url shouldn't contain any whitespaces
                        urlValue.takeIf { !it.contains(" ") } ?: run {
                            urlValue = urlValue.trimIndent()
                            urlValue
                        }
                    )?.let { tileUrlInfo ->
                        // TODO("Add support for online vector tile provider.")
                        coroutineScope.launch {
                            val rasterImage =
                                coroutineScope.async { viewModel.getTileImage(tileUrlInfo.url) }

                            viewModel.saveOnlineRasterTileProvider(
                                CustomTileProvider(
                                    CustomTileProviderType.Raster.Online(
                                        name = sourceName,
                                        url = tileUrlInfo.url,
                                        tileFileFormat = tileUrlInfo.tileFileFormat,
                                        minZoom = rangeSliderState.activeRangeStart.toInt(),
                                        maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                        tileSize = tileSize,
                                        image = rasterImage.await()
                                    )
                                )
                            )

                            onNavigateBack()
                        }
                    } ?: run {
                        isUrlValid = false
                    }
                }

                1 -> {
                    if (tileSize == -1 || sourceName.isBlank()) return@CustomTileSourceComposedDialogContent

                    coroutineScope.launch {
                        isLoading = true

                        fileUri?.let { uriString ->
                            copyFileToFolder(
                                contentResolver = context.contentResolver,
                                fileUri = Uri.parse(uriString),
                                destinationFolder = activity.obbDir,
                                fileName = queryName(
                                    context.contentResolver,
                                    Uri.parse(uriString)
                                ) ?: "custom_tiles"
                            ).let {
                                isLoading = false

                                if (it != null) {
                                    val format = MbTilesLoader.getFormat(it)
                                    val minMaxZoom = MbTilesLoader.getMinMaxZoom(it)

                                    when (format) {
                                        MbTileType.Vector -> {
                                            viewModel.saveOfflineVectorTileProvider(
                                                CustomTileProvider(
                                                    CustomTileProviderType.Vector.Offline(
                                                        name = sourceName,
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
                                                        name = sourceName,
                                                        minZoom = minMaxZoom.first,
                                                        maxZoom = minMaxZoom.second,
                                                        tileSize = tileSize.toInt(),
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
        },
        onNavigateBack = { onNavigateBack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomTileSourceComposedDialogContent(
    sourceName: String,
    urlValue: String,
    tileSize: Int,
    pagerState: PagerState,
    isLoading: Boolean,
    snackBarHostState: SnackbarHostState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    rangeSliderState: RangeSliderState,
    modifier: Modifier = Modifier,
    isUrlValid: Boolean,
    onTabClick: (Int) -> Unit,
    onSourceNameValueChange: (String) -> Unit,
    onUrlValueChange: (String) -> Unit,
    onToggleRangeSliderVisibility: () -> Unit,
    onTileSizeValueChange: (Int) -> Unit,
    onLaunchFileManager: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            titleRes = R.string.custom_tile_source,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationIconClick = { onNavigateBack() }
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
                        onTabClick(0)
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
                        onTabClick(1)
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
                                sourceName = sourceName,
                                urlValue = urlValue,
                                minZoom = rangeSliderState.activeRangeStart.toInt(),
                                maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                tileSize = tileSize,
                                isUrlValid = isUrlValid,
                                rangeSliderState = rangeSliderState,
                                bottomSheetScaffoldState = bottomSheetScaffoldState,
                                onSourceNameValueChange = { onSourceNameValueChange(it) },
                                onUrlValueChange = { onUrlValueChange(it) },
                                onToggleRangeSliderVisibility = { onToggleRangeSliderVisibility() },
                                onTileSizeValueChange = { onTileSizeValueChange(it) },
                                onDoneButtonClick = { onDoneButtonClick() }
                            )
                        }

                        1 -> {
                            OfflineTileSourceContent(
                                sourceName = sourceName,
                                tileSize = tileSize,
                                onSourceNameValueChange = { onSourceNameValueChange(it) },
                                onTileSizeValueChange = { onTileSizeValueChange(it) },
                                onLaunchFileManager = { onLaunchFileManager() },
                                onDoneButtonClick = { onDoneButtonClick() }
                            )
                        }
                    }

                    SnackbarHost(
                        hostState = snackBarHostState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )

                    IndeterminateCircularIndicator(
                        isShowed = isLoading,
                        size = 48.dp,
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    ) {
                        Text(text = "Loading...")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineTileSourceContent(
    sourceName: String,
    urlValue: String,
    minZoom: Int,
    maxZoom: Int,
    tileSize: Int,
    isUrlValid: Boolean,
    rangeSliderState: RangeSliderState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onSourceNameValueChange: (String) -> Unit,
    onUrlValueChange: (String) -> Unit,
    onToggleRangeSliderVisibility: () -> Unit,
    onTileSizeValueChange: (Int) -> Unit,
    onDoneButtonClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CustomTileSourceTextFields(
                sourceName = sourceName,
                urlValue = urlValue,
                isUrlValid = isUrlValid,
                onSourceNameValueChange = { onSourceNameValueChange(it) },
                onUrlValueChange = { onUrlValueChange(it) }
            )

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            ZoomLevels(
                minZoom = minZoom,
                maxZoom = maxZoom,
                onClick = {
                    onToggleRangeSliderVisibility()
                }
            )

            Divider(
                thickness = 1,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )

            TileSizeChips(tileSize = tileSize) {
                onTileSizeValueChange(it)
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
                    onClick = { onDoneButtonClick() }
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
    sourceName: String,
    tileSize: Int,
    onSourceNameValueChange: (String) -> Unit,
    onTileSizeValueChange: (Int) -> Unit,
    onLaunchFileManager: () -> Unit,
    onDoneButtonClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            value = sourceName,
            onValueChange = { onSourceNameValueChange(it) },
            label = {
                Text(
                    text = stringResource(id = R.string.name)
                )
            }
        )

        Divider(
            thickness = 1,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )

        TileSizeChips(tileSize = tileSize) {
            onTileSizeValueChange(it)
        }

        Divider(
            thickness = 1,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )

        Button(onClick = { onLaunchFileManager() }, enabled = tileSize != -1) {
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
                onClick = { onDoneButtonClick() }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun CustomTileSourceComposedDialogContentPreview() {
    CustomTileSourceComposedDialogContent(
        sourceName = "",
        urlValue = "",
        tileSize = -1,
        isLoading = false,
        pagerState = rememberPagerState(pageCount = { 2 }),
        snackBarHostState = remember {
            SnackbarHostState()
        },
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        rangeSliderState = remember {
            RangeSliderState()
        },
        isUrlValid = true,
        onTabClick = {},
        onSourceNameValueChange = {},
        onUrlValueChange = {},
        onToggleRangeSliderVisibility = {},
        onTileSizeValueChange = {},
        onLaunchFileManager = {},
        onDoneButtonClick = {},
        onNavigateBack = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun OnlineTileSourceContentPreview() {
    OnlineTileSourceContent(
        sourceName = "",
        urlValue = "",
        tileSize = -1,
        onSourceNameValueChange = {},
        onUrlValueChange = {},
        onToggleRangeSliderVisibility = {},
        minZoom = 0,
        maxZoom = 24,
        isUrlValid = true,
        rangeSliderState = remember {
            RangeSliderState()
        },
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        onTileSizeValueChange = {},
        onDoneButtonClick = {}
    )
}

@Composable
@Preview(showBackground = true)
fun OfflineTileSourceContentPreview() {
    OfflineTileSourceContent(
        sourceName = "",
        tileSize = -1,
        onSourceNameValueChange = {},
        onTileSizeValueChange = {},
        onLaunchFileManager = {},
        onDoneButtonClick = {}
    )
}

