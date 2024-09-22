package com.bytecause.custom_tile_provider.ui.screen

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.nautichart.features.custom_tile_provider.databinding.CustomTileSourceComposedDialogBinding
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.KeyboardUtils
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CustomTileSourceComposedDialog :
    Fragment(com.bytecause.nautichart.features.custom_tile_provider.R.layout.custom_tile_source_composed_dialog) {

    private val binding by viewBinding(
        CustomTileSourceComposedDialogBinding::bind
    )

    private var isKeyboardVisible: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        KeyboardUtils.addKeyboardToggleListener(requireActivity()) { isVisible ->
            isKeyboardVisible = isVisible
        }

        binding.customTileSourceDialog.setContent {
            AppTheme {
                CustomTileSourceComposedDialogScreen(
                    onNavigateBack = {
                        if (isKeyboardVisible) {
                            KeyboardUtils.forceCloseKeyboard(requireView())
                        }
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KeyboardUtils.removeAllKeyboardToggleListeners()
    }
}

@Composable
fun CustomTileSourceComposedDialogScreen(
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    val coroutineScope = rememberCoroutineScope()

    CustomTileSourceComposedDialogContent(
        pagerState = pagerState,
        onScrollToPage = { page ->
            coroutineScope.launch {
                pagerState.animateScrollToPage(page)
            }
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTileSourceComposedDialogContent(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onScrollToPage: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {

    Scaffold(topBar = {
        TopAppBar(
            titleRes = R.string.custom_tile_source,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationIconClick = onNavigateBack
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
                    onClick = { onScrollToPage(0) }
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
                    onClick = { onScrollToPage(1) }
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxWidth(),
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> {
                        OnlineTileSourceScreen(
                            pagerState = pagerState,
                            onNavigateBack = onNavigateBack
                        )
                    }

                    1 -> {
                        OfflineTileSourceScreen(
                            pagerState = pagerState,
                            onNavigateBack = onNavigateBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun CustomTileSourceComposedDialogContentPreview() {
    CustomTileSourceComposedDialogContent(
        pagerState = rememberPagerState(pageCount = { 2 }),
        onScrollToPage = {},
        onNavigateBack = {}
    )
}

