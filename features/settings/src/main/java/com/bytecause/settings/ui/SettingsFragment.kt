package com.bytecause.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bytecause.features.settings.R
import com.bytecause.features.settings.databinding.SettingsFragmentLayoutBinding
import com.bytecause.presentation.components.compose.TopAppBar
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.delegates.viewBinding

class SettingsFragment : Fragment(R.layout.settings_fragment_layout) {

    private val binding by viewBinding(SettingsFragmentLayoutBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsComposeContent.setContent {
            AppTheme {
                SettingsScreen(
                    onNavigateBack = {
                        findNavController().popBackStack()
                    },
                    onNavigateToCacheManagement = {
                        findNavController().navigate(R.id.cacheManagementFragment)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, onNavigateToCacheManagement: () -> Unit) {
    SettingsContent(
        onNavigateBack = onNavigateBack,
        onNavigateToCacheManagement = onNavigateToCacheManagement
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(onNavigateBack: () -> Unit, onNavigateToCacheManagement: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = com.bytecause.core.resources.R.string.settings,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCacheManagement() }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = com.bytecause.core.resources.R.drawable.cache),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = com.bytecause.core.resources.R.string.cache_management),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsScreen(
        onNavigateBack = {},
        onNavigateToCacheManagement = {}
    )
}