package com.bytecause.nautichart.data.cache

import android.content.Context
import android.os.Environment
import android.util.Log
import com.bytecause.nautichart.util.basePath
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException
import org.osmdroid.views.MapView
import java.io.File

private lateinit var mgr: CacheManager

private const val TAG = "download tiles"

class CacheDownloaderArchive(
    private val minZoom: Int,
    private val maxZoom: Int,
    private val map: MapView,
    private val context: Context
) {

    fun downloadTile(start: Boolean) {
        val archivePath =
            context.basePath() + File.separator + "files" + File.separator + "archives"

        val dir = File(archivePath)

        if (start) {
            if (!dir.exists()) {
                dir.mkdir()
            }
            val outputName = archivePath + File.separator + "archive.sqlite"
            val writer = SqliteArchiveTileWriter(outputName)

            try {
                mgr = CacheManager(map, writer)
            } catch (ex: TileSourcePolicyException) {
                Log.e(TAG, ex.message!!)
                return
            }
        }
        val bb = map.boundingBox
        mgr.downloadAreaAsync(context, bb, minZoom, maxZoom)
    }
}