package com.bytecause.map.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.bytecause.data.services.Actions
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.SaveRouteTrackDialogLayoutBinding
import com.bytecause.map.services.TrackRouteService
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.context.getActivity
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmStopRouteTrackComposeDialog :
    DialogFragment(R.layout.save_route_track_dialog_layout) {

    private val binding by viewBinding(SaveRouteTrackDialogLayoutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveRouteTrackComposeView.setContent {
            AppTheme {
                ConfirmStopRouteTrackScreen(onDismiss = { dismiss() })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}

@Composable
private fun ConfirmStopRouteTrackScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors()
            .copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 10.dp)
        ) {
            Text(
                text = stringResource(com.bytecause.core.resources.R.string.stop_tracking),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(com.bytecause.core.resources.R.string.dismiss),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() })

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(com.bytecause.core.resources.R.string.discard),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        Intent(context, TrackRouteService::class.java).apply {
                            action = Actions.STOP.toString()
                            putExtra(TrackRouteService.DISCARD, true)
                            context.startService(this)
                        }
                        onDismiss()
                    })

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = stringResource(com.bytecause.core.resources.R.string.save),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                        (context.getActivity() as? AppCompatActivity)?.supportFragmentManager?.let {
                            SaveRouteTrackComposedDialog().run {
                                show(it, tag)
                            }
                        }
                    })
            }
        }
    }
}

@Composable
@Preview
private fun ConfirmStopRouteTrackScreenPreview() {
    ConfirmStopRouteTrackScreen(onDismiss = {})
}
