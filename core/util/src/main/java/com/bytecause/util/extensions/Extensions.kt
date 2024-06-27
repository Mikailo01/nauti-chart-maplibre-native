package com.bytecause.util.extensions

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

// Tag name for logging.
fun Fragment.TAG(): String? {
    return this::class.simpleName
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
    when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else ->
            @Suppress("DEPRECATION")
            getParcelable(key)
                as? T
    }

inline fun <reified T> TAG(clazz: T): String? {
    return T::class.simpleName
}

fun Context.basePath(): String =
    Environment.getExternalStorageDirectory().absolutePath + File.separator + "Android" + File.separator + "data" +
        File.separator + applicationContext.packageName + File.separator

fun Context.tilesPath(): String = basePath() + "files" + File.separator + "tiles"
