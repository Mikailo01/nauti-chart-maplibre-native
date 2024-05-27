package com.bytecause.nautichart.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileUtil {
    suspend fun copyFileToFolder(
        contentResolver: ContentResolver,
        fileUri: Uri,
        destinationFolder: File,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create the destination file in the specified folder
            val destinationFile =
                File(destinationFolder, fileName)

            // Open InputStream from the file URI
            val inputStream: InputStream? = contentResolver.openInputStream(fileUri)

            // Create OutputStream to the destination file
            val outputStream: OutputStream = FileOutputStream(destinationFile)

            // Buffer for reading and writing
            val buffer = ByteArray(1024)
            var length: Int

            // Copy the data
            inputStream?.use { stream ->
                outputStream.use { outputStream ->
                    while (stream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }

            Log.d("FileCopy", "File copied successfully to ${destinationFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("FileCopy", "Error copying file: ${e.message}", e)
            false
        }
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