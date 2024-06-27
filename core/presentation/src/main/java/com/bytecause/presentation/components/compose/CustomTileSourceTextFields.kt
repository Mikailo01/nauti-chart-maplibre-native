package com.bytecause.presentation.components.compose

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomTileSourceTextFields(
    sourceName: String,
    modifier: Modifier = Modifier,
    urlValue: String? = null,
    isUrlValid: Boolean = true,
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
            isError = !isUrlValid,
            label = {
                Text(
                    text = "Url"
                )
            },
            supportingText = {
                if (!isUrlValid) {
                    Text(text = "Url is not in valid format.")
                }
            }
        )
    }
}