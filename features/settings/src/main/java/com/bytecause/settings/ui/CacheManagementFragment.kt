package com.bytecause.settings.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.features.settings.R
import com.bytecause.features.settings.databinding.CacheManagementLayoutBinding
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.settings.ui.event.CacheManagementEffect
import com.bytecause.settings.ui.event.CacheManagementEvent
import com.bytecause.settings.ui.state.CacheManagementState
import com.bytecause.settings.ui.viewmodel.CacheManagementViewModel
import com.bytecause.util.delegates.viewBinding
import java.util.Date

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

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CacheManagementEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    CacheManagementContent(
        state = state, onEvent =
        viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheManagementContent(
    state: CacheManagementState,
    onEvent: (CacheManagementEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = com.bytecause.core.resources.R.string.cache_management,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = { onEvent(CacheManagementEvent.OnNavigateBack) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
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
                ) {
                    Row {
                        Text(
                            text = "Harbours",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "-",
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Update interval",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "1 week") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )

                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "2 weeks") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )

                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "1 month") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { }, modifier = Modifier.wrapContentHeight()) {
                            Text(text = "Force update", fontSize = 14.sp)
                        }
                        OutlinedButton(onClick = { }, modifier = Modifier.wrapContentHeight()) {
                            Text(text = "Clear", fontSize = 14.sp, color = Color.Red)
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
                            text = "Vessels",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "-",
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
                        OutlinedButton(onClick = { }, modifier = Modifier.wrapContentHeight()) {
                            Text(text = "Clear", fontSize = 14.sp, color = Color.Red)
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
                            text = "POIs",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "-",
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Update interval",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "1 week") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )

                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "2 weeks") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )

                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "1 month") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.time_left),
                                    contentDescription = null
                                )
                            },
                            border = BorderStroke(
                                2.dp,
                                colorResource(id = com.bytecause.core.resources.R.color.md_theme_secondaryContainer_mediumContrast)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { }, modifier = Modifier.wrapContentHeight()) {
                            Text(text = "Force update", fontSize = 14.sp)
                        }
                        OutlinedButton(onClick = { }, modifier = Modifier.wrapContentHeight()) {
                            Text(text = "Clear", fontSize = 14.sp, color = Color.Red)
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(emptyList<String>()) { item ->
                            DownloadedRegionItem(regionName = item, timeStamp = 0L)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedRegionItem(regionName: String, timeStamp: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
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
}

@Composable
@Preview
fun CacheManagementScreenPreview() {
    CacheManagementScreen(onNavigateBack = {})
}

@Composable
@Preview(showBackground = true)
fun DownloadedRegionItemPreview() {
    DownloadedRegionItem("Jihomoravský kraj", 1724861327000L)
}