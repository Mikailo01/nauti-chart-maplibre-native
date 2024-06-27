package com.bytecause.presentation.components.compose

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnnotatedString(
    @StringRes textRes: Int,
    vararg args: String
) {
    val annotatedString = buildAnnotatedString {
        val text = stringResource(id = textRes, *args)

        append(text)

        // Apply style for both args
        args.forEach { arg ->
            var startIndex = text.indexOf(arg)
            while (startIndex >= 0) {
                val endIndex = startIndex + arg.length
                addStyle(
                    style = SpanStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ), start = startIndex, end = endIndex
                )
                // This ensures that the style will be applied to second string arg, because if values
                // are the same then it would be applied only to the first one
                startIndex = text.indexOf(arg, startIndex + 1)
            }
        }
    }

    Text(text = annotatedString)
}