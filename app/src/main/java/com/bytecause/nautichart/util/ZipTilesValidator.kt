package com.bytecause.nautichart.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.anggrayudi.storage.extension.openInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ZipTilesValidator {

    suspend fun isZipValid(uri: Uri, context: Context): TileFileValidationResult =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                uri.openInputStream(context).use { inputStream ->
                    val zipInputStream = ZipInputStream(inputStream)
                    val topLevelEntries = mutableSetOf<String>()
                    val childDirectories = mutableMapOf<String, MutableSet<String>>()

                    var entry: ZipEntry?
                    while (zipInputStream.nextEntry.also { entry = it } != null) {

                        entry?.let {
                            val entryName = it.name
                            val entryParts = entryName.split("/")

                            if (entryParts.size > 1) {
                                val topLevelEntry = entryParts[0]
                                topLevelEntries.add(topLevelEntry)

                                if (entryParts.size > 2 || it.isDirectory) {
                                    val parentDir = entryParts.dropLast(1).joinToString("/")
                                    val childDir =
                                        entryParts.take(entryParts.size - 1).joinToString("/")

                                    if (parentDir !in childDirectories) {
                                        childDirectories[parentDir] = mutableSetOf()
                                    }
                                    childDirectories[parentDir]?.add(childDir)
                                }
                            }
                        }
                    }

                    if (topLevelEntries.size == 1) {
                        val parentFolder = topLevelEntries.first()
                        val hasChildDirectories =
                            childDirectories[parentFolder]?.isNotEmpty() ?: false

                        Log.d("idk", "Single parent folder: $parentFolder")
                        Log.d("idk", "Has child directories: $hasChildDirectories")

                        if (hasChildDirectories) {
                            continuation.resume(TileFileValidationResult(valid = true))
                        } else {
                            continuation.resume(
                                TileFileValidationResult(
                                    valid = false,
                                    invalid = InvalidTileFile.ZipFileInvalidSchema
                                )
                            )
                        }
                    } else {
                        Log.d("idk", "More than one top-level entry or no entries")
                        continuation.resume(
                            TileFileValidationResult(
                                valid = false,
                                invalid = InvalidTileFile.ZipFileInvalidSchema
                            )
                        )
                    }
                }
            }
        }
}

data class TileFileValidationResult(
    val valid: Boolean? = null,
    val invalid: InvalidTileFile? = null
)

sealed interface InvalidTileFile {
    data object ZipFileTooLarge : InvalidTileFile
    data object ZipFileInvalidSchema : InvalidTileFile
}