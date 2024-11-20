package com.bytecause.presentation.components.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.core.resources.R

@Composable
fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int = R.string.are_you_sure,
    @StringRes confirmText: Int = R.string.confirm,
    @StringRes dismissText: Int = R.string.dismiss,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {

    androidx.compose.ui.window.Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 10.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                content()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { onDismiss() }) {
                    Text(text = stringResource(id = dismissText))
                }

                Button(
                    onClick = { onConfirm() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(id = confirmText))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ConfirmationDialogPreview() {
    ConfirmationDialog(onDismiss = { }, onConfirm = { }) {
        Text(text = stringResource(id = R.string.preview))
    }
}