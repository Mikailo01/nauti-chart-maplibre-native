package com.bytecause.first_run.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.domain.model.RegionModel
import com.bytecause.features.first_run.databinding.SelectRegionDialogLayoutBinding
import com.bytecause.first_run.ui.viewmodel.FirstRunSharedViewModel
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.delegates.viewBinding
import java.util.Locale

class SelectRegionComposedDialog : DialogFragment() {

    private val binding by viewBinding(
        SelectRegionDialogLayoutBinding::inflate
    )

    private val sharedViewModel by activityViewModels<FirstRunSharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        isCancelable = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectRegionDialogComposeView.setContent {
            AppTheme {
                SelectRegionComposedDialogScreen(
                    sharedViewModel = sharedViewModel,
                    onNavigateBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun SelectRegionComposedDialogScreen(
    sharedViewModel: FirstRunSharedViewModel,
    onNavigateBack: () -> Unit
) {
    val regions by sharedViewModel.regionsSharedFlow.collectAsStateWithLifecycle()

    SelectRegionComposedDialogContent(
        regions = regions,
        onRegionClick = {
            sharedViewModel.setSelectedRegion(it)
            onNavigateBack()
        }
    )
}

@Composable
fun SelectRegionComposedDialogContent(
    regions: List<RegionModel>,
    onRegionClick: (RegionModel) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            colors = CardDefaults.cardColors()
                .copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
        ) {
            Column(
                modifier = Modifier.padding(top = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Select region", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                val locale = Locale.getDefault().language

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(regions, key = { it.id }) { item ->

                        val regionName = item.names["name:$locale"]
                            ?: item.names["name:en"]
                            ?: item.names["name"]

                        regionName?.let { name ->
                            RegionItem(
                                regionName = name,
                                regionId = item.id,
                                onItemClick = { if (it == item.id) onRegionClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegionItem(
    regionName: String,
    regionId: Int,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(regionId) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.earth_24),
            contentDescription = null,
            modifier = Modifier.padding(20.dp)
        )
        Text(text = regionName)
    }
}

@Composable
@Preview
fun SelectRegionComposedDialogContentPreview() {
    SelectRegionComposedDialogContent(regions = emptyList(), onRegionClick = {})
}