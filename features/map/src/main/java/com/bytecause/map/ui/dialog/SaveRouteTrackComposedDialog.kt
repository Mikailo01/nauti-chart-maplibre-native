package com.bytecause.map.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaveRouteTrackComposedDialog :
    DialogFragment(R.layout.save_route_track_dialog_layout) {

    private val binding by viewBinding(SaveRouteTrackDialogLayoutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveRouteTrackComposeView.setContent {
            AppTheme {
                StartRouteTrackScreen(onDismiss = { dismiss() })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}

@Composable
private fun StartRouteTrackScreen(onDismiss: () -> Unit) {

    var name by rememberSaveable {
        mutableStateOf("")
    }

    var description by rememberSaveable {
        mutableStateOf("")
    }

    var isNameError by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors()
            .copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text = stringResource(com.bytecause.core.resources.R.string.save_route),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (isNameError) isNameError = false
                },
                isError = isNameError,
                supportingText = {
                    if (isNameError) {
                        Text(text = stringResource(com.bytecause.core.resources.R.string.cannot_be_empty))
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(com.bytecause.core.resources.R.drawable.important),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(6.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(com.bytecause.core.resources.R.string.name),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = {
                    Text(
                        text = stringResource(com.bytecause.core.resources.R.string.description),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )

            OutlinedButton(
                onClick = {
                    if (name.isNotBlank()) {
                        Intent(context, TrackRouteService::class.java).apply {
                            action = Actions.STOP.toString()
                            putExtra(TrackRouteService.NAME, name)
                            putExtra(TrackRouteService.DESCRIPTION, description)
                            context.startService(this)
                        }
                        onDismiss()
                    } else {
                        isNameError = name.isBlank()
                    }
                },
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = stringResource(com.bytecause.core.resources.R.string.save),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun StartRouteTrackScreenPreview() {
    StartRouteTrackScreen(onDismiss = {})
}