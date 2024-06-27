package com.bytecause.util.context

import android.Manifest
import android.R.attr
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.format.Formatter
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.graphics.drawable.toBitmap
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageId

fun Context.getProgressBarDrawable(): Drawable {
    val value = TypedValue()
    theme.resolveAttribute(attr.progressBarStyleSmall, value, false)
    val progressBarStyle = value.data
    val attributes = intArrayOf(attr.indeterminateDrawable)
    val array = obtainStyledAttributes(progressBarStyle, attributes)
    val drawable = array.getDrawableOrThrow(0)
    array.recycle()
    return drawable
}

fun Context.modifyDrawableScale(drawable: Drawable?, scaleFactor: Float): Drawable? {
    if (drawable == null) return null

    val bitmap = drawable.toBitmap()
    val scaleX = (bitmap.width * scaleFactor).toInt()
    val scaleY = (bitmap.height * scaleFactor).toInt()
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleX, scaleY, false)

    return BitmapDrawable(this.resources, scaledBitmap)
}

fun Context.storageAvailable(): Map<String, Int> {
    val percent: Int
    val totalBytesOfPrimary =
        DocumentFileCompat.getStorageCapacity(this, StorageId.PRIMARY)
    val freeBytesOfPrimary =
        DocumentFileCompat.getFreeSpace(this, StorageId.PRIMARY)

    percent = ((100 * (totalBytesOfPrimary - freeBytesOfPrimary)) / totalBytesOfPrimary).toInt()

    return mapOf(Formatter.formatFileSize(this, freeBytesOfPrimary) to percent)
}

fun Context.showKeyboard(view: View?) {
    view ?: return

    val keyboard =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view.requestFocus()
    view.postDelayed({
        keyboard.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }, 200)
}

fun Context.hideKeyboard(view: View?) {
    view ?: return

    val inputMethodManager =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.isLocationPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED