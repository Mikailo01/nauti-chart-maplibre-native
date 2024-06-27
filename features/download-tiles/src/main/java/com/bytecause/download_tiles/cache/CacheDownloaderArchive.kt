package com.bytecause.download_tiles.cache

import android.content.Context
import android.util.Log
import com.bytecause.util.extensions.TAG
import com.bytecause.util.extensions.basePath
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException
import org.osmdroid.views.MapView
import java.io.File

class CacheDownloaderArchive(
    private val minZoom: Int,
    private val maxZoom: Int,
    private val map: MapView,
    private val context: Context
) {

    private lateinit var mgr: CacheManager

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
                Log.e(TAG(this), ex.message!!)
                return
            }
        }
        val bb = map.boundingBox
        mgr.downloadAreaAsync(context, bb, minZoom, maxZoom)
    }
}