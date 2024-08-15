package com.bytecause.util.string

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {

    data class DynamicString(val value: String) : UiText()

    data object Empty : UiText()

    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    fun asString(context: Context?): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> context?.getString(resId, *args).orEmpty()
        }
    }
}