package com.bytecause.util.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    fun Context.offlineTilesDir(): String = obbDir.absolutePath

    suspend fun copyFileToFolder(
        contentResolver: ContentResolver,
        fileUri: Uri,
        destinationFolder: String,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Create the destination file in the specified folder
            val destinationFile =
                File(destinationFolder, fileName)

            // Buffer for reading and writing
            val buffer = ByteArray(1024)
            var length: Int

            // Copy the data
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                        ensureActive()
                    }
                }
            }

            Timber.tag("FileCopy").d("File copied successfully to %s", destinationFile.absolutePath)
            destinationFile
        } catch (e: Exception) {
            Timber.tag("FileCopy").e(e, "Error copying file: %s", e.message)

            // Cleanup operation
            withContext(NonCancellable) {
                deleteFileFromFolder(destinationFolder, fileName)
            }

            null
        }
    }

    suspend fun deleteFileFromFolder(
        destinationFolder: String,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val targetFolder = File(destinationFolder, fileName)
            targetFolder.delete()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkFilenameExists(fileName: String, destinationFolder: String): Boolean =
        withContext(Dispatchers.IO) {
            File(destinationFolder, fileName).exists()
        }

    fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor =
            resolver.query(uri, null, null, null, null) ?: return null
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}