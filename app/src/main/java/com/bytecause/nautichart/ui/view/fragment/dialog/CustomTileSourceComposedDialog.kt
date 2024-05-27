package com.bytecause.nautichart.ui.view.fragment.dialog

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.bytecause.nautichart.CustomOfflineTileSource
import com.bytecause.nautichart.CustomOnlineTileSource
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.CustomTileSourceComposedDialogBinding
import com.bytecause.nautichart.tilesources.AnalyzeCustomOnlineTileProvider.extractTileUrlAttrs
import com.bytecause.nautichart.ui.compose.components.CustomOutlinedButton
import com.bytecause.nautichart.ui.compose.components.CustomRangeSlider
import com.bytecause.nautichart.ui.compose.components.CustomTileSourceTextFields
import com.bytecause.nautichart.ui.compose.components.Divider
import com.bytecause.nautichart.ui.compose.components.IndeterminateCircularIndicator
import com.bytecause.nautichart.ui.compose.components.TopAppBar
import com.bytecause.nautichart.ui.compose.components.ZoomLevels
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.CustomTileSourceDialogViewModel
import com.bytecause.nautichart.util.FileUtil.copyFileToFolder
import com.bytecause.nautichart.util.FileUtil.queryName
import com.bytecause.nautichart.util.InvalidTileFile
import com.bytecause.nautichart.util.TileFileValidationResult
import com.bytecause.nautichart.util.ZipTilesValidator
import com.bytecause.nautichart.util.then
import com.bytecause.nautichart.util.tilesPath
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


enum class TileFileFormat {
    Zip,
    SQLite,
    MBTiles
}

private const val MIN_ZOOM = 0f
private const val MAX_ZOOM = 26f

@AndroidEntryPoint
class CustomTileSourceComposedDialog : DialogFragment() {

    private val binding by viewBinding(CustomTileSourceComposedDialogBinding::inflate)

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
        mutableStateOf("")
    }

    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }

    var fileValidationResult by remember {
        mutableStateOf(TileFileValidationResult())
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val radioOptions = TileFileFormat.entries.toList()
    var selectedFileFormat by rememberSaveable { mutableStateOf(radioOptions[0]) }

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

        coroutineScope.launch(Dispatchers.IO) {
            if (selectedFileFormat == TileFileFormat.Zip) {
                isLoading = true
                ZipTilesValidator.isZipValid(uri, context).let {
                    isLoading = false
                    fileValidationResult = it
                }
            } else {
                fileValidationResult = fileValidationResult.copy(valid = true, invalid = null)
                fileUri = uri.toString()
            }
        }
    }

    LaunchedEffect(key1 = fileValidationResult) {
        when {
            fileValidationResult.valid == true && fileValidationResult.invalid == null -> {
                snackBarHostState.showSnackbar("Valid")
            }

            fileValidationResult.valid == false -> {
                fileValidationResult.invalid?.let { invalidTileFile ->
                    when (invalidTileFile) {
                        InvalidTileFile.ZipFileInvalidSchema -> {
                            snackBarHostState.showSnackbar("Invalid schema")
                        }

                        InvalidTileFile.ZipFileTooLarge -> {
                            snackBarHostState.showSnackbar("Zip file too large")
                        }
                    }
                }
            }
        }
    }

    CustomTileSourceComposedDialogContent(
        sourceName = sourceName,
        urlValue = urlValue,
        tileSizeValue = tileSize,
        pagerState = pagerState,
        fileValidationResult = fileValidationResult,
        isLoading = isLoading,
        radioOptions = radioOptions,
        selectedFileFormat = selectedFileFormat,
        snackBarHostState = snackBarHostState,
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        rangeSliderState = rangeSliderState,
        onSourceNameValueChange = { sourceName = it },
        onUrlValueChange = { urlValue = it },
        onToggleRangeSliderVisibility = {
            coroutineScope.launch {
                if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                } else bottomSheetScaffoldState.bottomSheetState.show()
            }
        },
        onTileSizeValueChange = { tileSize = it },
        onFileFormatSelect = { selectedFileFormat = it },
        onLaunchFileManager = {
            launcher.launch(
                when (selectedFileFormat) {
                    TileFileFormat.Zip -> {
                        "application/zip"
                    }

                    TileFileFormat.SQLite -> {
                        "application/octet-stream"
                    }

                    TileFileFormat.MBTiles -> {
                        "application/octet-stream"
                    }
                }
            )
        },
        onDoneButtonClick = {
            when (pagerState.currentPage) {
                0 -> {
                    if (sourceName.isBlank() || urlValue.isBlank() || tileSize.isBlank()) return@CustomTileSourceComposedDialogContent

                    extractTileUrlAttrs(
                        // Url shouldn't contain any whitespaces
                        urlValue.takeIf { !it.contains(" ") } ?: run {
                            urlValue = urlValue.trimIndent()
                            urlValue
                        }
                    )?.let { tileUrlInfo ->
                        viewModel.saveOnlineTileProvider(
                            CustomOnlineTileSource.newBuilder().apply {
                                setName(sourceName)
                                setUrl(tileUrlInfo.baseUrl)
                                setTileFileFormat(tileUrlInfo.tileFileFormat)
                                setSchema(tileUrlInfo.schema)
                                setMinZoom(rangeSliderState.activeRangeStart.toInt())
                                setMaxZoom(rangeSliderState.activeRangeEnd.toInt())
                                setTileSize(tileSize.toInt())
                            }.build()
                        )

                        onNavigateBack()
                    }
                }

                1 -> {
                    if (tileSize.isBlank()) return@CustomTileSourceComposedDialogContent

                    val targetPath = File(context.tilesPath()).apply {
                        if (!exists()) mkdirs()
                    }

                    coroutineScope.launch {
                        isLoading = true

                        fileUri?.let { uriString ->
                            copyFileToFolder(
                                contentResolver = context.contentResolver,
                                fileUri = Uri.parse(uriString),
                                destinationFolder = targetPath,
                                fileName = queryName(
                                    context.contentResolver,
                                    Uri.parse(uriString)
                                )?.also { sourceName = it.split(".")[0] }
                                    ?: "custom_tiles".also { sourceName = it }
                            ).let {
                                isLoading = false

                                if (it) {
                                    viewModel.saveOfflineTileProvider(
                                        CustomOfflineTileSource.newBuilder().apply {
                                            setName(sourceName)
                                            setMinZoom(rangeSliderState.activeRangeStart.toInt())
                                            setMaxZoom(rangeSliderState.activeRangeEnd.toInt())
                                            setTileSize(tileSize.toInt())
                                        }.build()
                                    )
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
    tileSizeValue: String,
    fileValidationResult: TileFileValidationResult,
    pagerState: PagerState,
    isLoading: Boolean,
    radioOptions: List<TileFileFormat>,
    selectedFileFormat: TileFileFormat,
    snackBarHostState: SnackbarHostState,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    rangeSliderState: RangeSliderState,
    modifier: Modifier = Modifier,
    onSourceNameValueChange: (String) -> Unit,
    onUrlValueChange: (String) -> Unit,
    onToggleRangeSliderVisibility: () -> Unit,
    onTileSizeValueChange: (String) -> Unit,
    onFileFormatSelect: (TileFileFormat) -> Unit,
    onLaunchFileManager: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

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
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
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
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxWidth(),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        when (page) {
                            0 -> {
                                OnlineTileSourceContent(
                                    sourceName = sourceName,
                                    urlValue = urlValue,
                                    minZoom = rangeSliderState.activeRangeStart.toInt(),
                                    maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                    tileSizeValue = tileSizeValue,
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
                                    tileSizeValue = tileSizeValue,
                                    minZoom = rangeSliderState.activeRangeStart.toInt(),
                                    maxZoom = rangeSliderState.activeRangeEnd.toInt(),
                                    tileFileValidationResult = fileValidationResult,
                                    radioOptions = radioOptions,
                                    selectedFileFormat = selectedFileFormat,
                                    onSourceNameValueChange = { onSourceNameValueChange(it) },
                                    onFileFormatSelect = { onFileFormatSelect(it) },
                                    onToggleRangeSliderVisibility = { onToggleRangeSliderVisibility() },
                                    onTileSizeValueChange = { onTileSizeValueChange(it) },
                                    onLaunchFileManager = { onLaunchFileManager() },
                                    onDoneButtonClick = { onDoneButtonClick() }
                                )
                            }
                        }
                    }

                    BottomSheetScaffold(
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

@Composable
fun OnlineTileSourceContent(
    sourceName: String,
    urlValue: String,
    minZoom: Int,
    maxZoom: Int,
    tileSizeValue: String,
    onSourceNameValueChange: (String) -> Unit,
    onUrlValueChange: (String) -> Unit,
    onToggleRangeSliderVisibility: () -> Unit,
    onTileSizeValueChange: (String) -> Unit,
    onDoneButtonClick: () -> Unit
) {

    CustomTileSourceTextFields(
        sourceName = sourceName,
        urlValue = urlValue,
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

    TextField(
        value = tileSizeValue,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_layers_24),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        onValueChange = {
            if (!it.isDigitsOnly()) return@TextField

            onTileSizeValueChange(it)
        },
        label = {
            Text(
                text = stringResource(
                    id = R.string.tile_size
                )
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width((LocalConfiguration.current.screenWidthDp / 2).dp)
    )

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

@Composable
fun OfflineTileSourceContent(
    sourceName: String,
    tileSizeValue: String,
    minZoom: Int,
    maxZoom: Int,
    tileFileValidationResult: TileFileValidationResult,
    radioOptions: List<TileFileFormat>,
    selectedFileFormat: TileFileFormat,
    onSourceNameValueChange: (String) -> Unit,
    onFileFormatSelect: (TileFileFormat) -> Unit,
    onToggleRangeSliderVisibility: () -> Unit,
    onTileSizeValueChange: (String) -> Unit,
    onLaunchFileManager: () -> Unit,
    onDoneButtonClick: () -> Unit
) {

    TextField(
        value = sourceName,
        onValueChange = { onSourceNameValueChange(it) },
        enabled = false,
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

    TextField(
        value = tileSizeValue,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_layers_24),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        onValueChange = {
            if (!it.isDigitsOnly()) return@TextField

            onTileSizeValueChange(it)
        },
        label = {
            Text(
                text = stringResource(
                    id = R.string.tile_size
                )
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width((LocalConfiguration.current.screenWidthDp / 2).dp)
    )

    Divider(
        thickness = 1,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    )

    Text(
        text = stringResource(id = R.string.choose_file_format),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )
    Row(Modifier.selectableGroup()) {
        radioOptions.forEach { format ->
            Column(
                modifier = Modifier.then(
                    format == TileFileFormat.SQLite,
                    onTrue = { padding(end = 10.dp) }),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = format.name, fontStyle = Italic)
                RadioButton(
                    selected = format == selectedFileFormat,
                    onClick = { onFileFormatSelect(format) },
                    modifier = Modifier.semantics {
                        contentDescription = "Localized Description"
                    }
                )
            }
        }
    }

    Button(onClick = { onLaunchFileManager() }, enabled = tileSizeValue.isNotBlank()) {
        Text(text = stringResource(id = R.string.browse_files))
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
            enabled = tileFileValidationResult.valid == true,
            onClick = { onDoneButtonClick() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun CustomTileSourceComposedDialogContentPreview() {
    CustomTileSourceComposedDialogContent(
        sourceName = "",
        urlValue = "",
        tileSizeValue = "",
        isLoading = false,
        fileValidationResult = TileFileValidationResult(true),
        pagerState = rememberPagerState(pageCount = { 2 }),
        radioOptions = emptyList(),
        selectedFileFormat = TileFileFormat.Zip,
        snackBarHostState = remember {
            SnackbarHostState()
        },
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        rangeSliderState = remember {
            RangeSliderState()
        },
        onSourceNameValueChange = {},
        onUrlValueChange = {},
        onToggleRangeSliderVisibility = {},
        onTileSizeValueChange = {},
        onFileFormatSelect = {},
        onLaunchFileManager = {},
        onDoneButtonClick = {},
        onNavigateBack = {}
    )
}

@Composable
@Preview(showBackground = true)
fun OnlineTileSourceContentPreview() {
    OnlineTileSourceContent(
        sourceName = "",
        urlValue = "",
        tileSizeValue = "",
        onSourceNameValueChange = {},
        onUrlValueChange = {},
        onToggleRangeSliderVisibility = {},
        minZoom = 0,
        maxZoom = 24,
        onTileSizeValueChange = {},
        onDoneButtonClick = {}
    )
}

@Composable
@Preview(showBackground = true)
fun OfflineTileSourceContentPreview() {
    OfflineTileSourceContent(
        sourceName = "",
        tileSizeValue = "",
        minZoom = 0,
        maxZoom = 26,
        tileFileValidationResult = TileFileValidationResult(true),
        radioOptions = TileFileFormat.entries.toList(),
        selectedFileFormat = TileFileFormat.Zip,
        onSourceNameValueChange = {},
        onFileFormatSelect = {},
        onToggleRangeSliderVisibility = {},
        onTileSizeValueChange = {},
        onLaunchFileManager = {},
        onDoneButtonClick = {}
    )
}

