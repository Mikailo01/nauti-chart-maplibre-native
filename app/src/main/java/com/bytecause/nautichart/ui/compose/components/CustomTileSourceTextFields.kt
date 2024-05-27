package com.bytecause.nautichart.ui.compose.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomTileSourceTextFields(
    sourceName: String,
    modifier: Modifier = Modifier,
    urlValue: String? = null,
    onSourceNameValueChange: (String) -> Unit,
    onUrlValueChange: (String) -> Unit = {}
) {

    TextField(
        value = sourceName,
        onValueChange = { onSourceNameValueChange(it) },
        label = {
            Text(
                text = "Name"
            )
        }
    )
    urlValue?.let { value ->
        TextField(
            value = value,
            onValueChange = { onUrlValueChange(it) },
            label = {
                Text(
                    text = "Url"
                )
            }
        )
    }
}