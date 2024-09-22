package com.bytecause.util.map

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.io.InputStreamReader

enum class MbTileType {
    Raster,
    Vector
}

object MbTilesLoader {

    fun getMbTileFileStyle(activity: Activity, filePath: String): File {
        val styleName = "bright.json"

        val styleJsonInputStream = activity.assets.open("bright.json")

        //Creating a new file to which to copy the json content to
        val dir = File(activity.filesDir.absolutePath)
        val styleFile = File(dir, styleName)
        //Copying the original JSON content to new file
        copyStreamToFile(styleJsonInputStream, styleFile)

        //Replacing placeholder with uri of the mbtiles file
        val newFileStr = styleFile.inputStream().readToString()
            .replace(
                "___FILE_URI___",
                "mbtiles:///${filePath}"
            )

        //Writing new content to file
        val gpxWriter = FileWriter(styleFile)
        val out = BufferedWriter(gpxWriter)
        out.write(newFileStr)
        out.close()

        return styleFile
    }

    private fun InputStream.readToString(): String {
        val r = BufferedReader(InputStreamReader(this))
        val total = StringBuilder("")
        var line: String?
        while (r.readLine().also { line = it } != null) {
            total.append(line).append('\n')
        }
        return total.toString()
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {

        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    fun getFormat(file: File): MbTileType {
        val openDatabase =
            SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        val formatCursor = openDatabase.query(
            "metadata",
            arrayOf("name", "value"),
            "name=?",
            arrayOf("format"),
            null,
            null,
            null,
        )
        formatCursor.moveToFirst()
        val format = formatCursor.getString(1)

        formatCursor.close()
        openDatabase.close()

        return when (format) {
            "pbf" -> MbTileType.Vector
            else -> MbTileType.Raster
        }
    }

    fun getMinMaxZoom(file: File): Pair<Int, Int> {
        val openDatabase =
            SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        val minZoomCursor = openDatabase.query(
            "metadata",
            arrayOf("name", "value"),
            "name=?",
            arrayOf("minzoom"),
            null,
            null,
            null,
        )
        minZoomCursor.moveToFirst()
        val minZoomLevel = minZoomCursor.getString(1)

        minZoomCursor.close()

        val maxZoomCursor = openDatabase.query(
            "metadata",
            arrayOf("name", "value"),
            "name=?",
            arrayOf("maxzoom"),
            null,
            null,
            null,
        )
        maxZoomCursor.moveToFirst()
        val maxZoomLevel = maxZoomCursor.getString(1)

        maxZoomCursor.close()

        openDatabase.close()

        return minZoomLevel.toInt() to maxZoomLevel.toInt()
    }
}