package com.bytecause.util.extensions

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.File
import kotlin.math.round

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

/**
 * Rounds the value to the first decimal place.
 */
fun Double.toFirstDecimal() = round(this * 10) / 10

/**
 * Rounds the result of applying a given transformation to this [Double] to the first decimal place.
 *
 * @param block A lambda function that transforms the [Double].
 * The result of this transformation will be rounded to one decimal place.
 * @return The transformed [Double] value rounded to one decimal place.
 */
fun Double.toFirstDecimal(block: (Double).() -> Double) = round(block() * 10) / 10

/**
 * Rounds the value to the first decimal place.
 */
fun Float.toFirstDecimal() = round(this * 10) / 10
