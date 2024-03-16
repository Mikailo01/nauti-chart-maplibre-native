package com.bytecause.nautichart.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.BoundingBox
import java.net.HttpURLConnection
import java.net.URL

class HarboursRemoteDataSource {

    private val baseUrl = "https://harbour.openseamap.org/getHarbours.php?"

    private val urlMap = hashMapOf(
        "TopLeftQuadrant" to listOf(
            "b=0&t=30&l=-120&r=-60",
            "b=0&t=30&l=-180&r=-120",
            "b=0&t=30&l=-60&r=0",
            "b=30&t=60&l=-60&r=0",
            "b=30&t=60&l=-120&r=-60",
            "b=30&t=60&l=-180&r=-120",
            "b=60&t=90&l=-60&r=0",
            "b=60&t=90&l=-120&r=-60",
            "b=60&t=90&l=-180&r=-120"
        ), // 4.
        "BottomLeftQuadrant" to "b=-90&t=0&l=-180&r=0", // 5.
        "BottomRightQuadrant" to "b=-90&t=0&l=0&r=180", // 6.
        "TopRightQuadrantSubQuadrants" to listOf(
            "b=22.5&t=33.75&l=0&r=90", // 1.
            "b=33.75&t=37.5&l=0&r=90", // 2.
            "b=37.5&t=45&l=22.5&r=90", // 3.
            "b=37.5&t=45&l=0&r=14", // 7.
            "b=37.5&t=45&l=14&r=22.5", // 8.
            "b=0&t=22.5&l=0&r=90", // 9.
            "b=0&t=90&l=90&r=180", // 10.
            "b=59&t=90&l=0&r=90", // 11.
            "b=45&t=51&l=0&r=90", // 12.
            "b=56&t=59&l=0&r=12", // 13.
            "b=56&t=59&l=12&r=90", // 14.
            "b=51&t=56&l=0&r=10", // 15.
            "b=51&t=56&l=10&r=90" // 16.
        )
    )

    suspend fun fetchTextFromUrls(): List<String> {
        return withContext(Dispatchers.IO) {
            val contentList = mutableListOf<String>()
            for ((_, value) in urlMap) {
                if (value is List<*>) {
                    // Iterate over the list
                    for (url in value) {
                        val modifiedUrl = modifyUrl(url as String)
                        val connection = URL(modifiedUrl).openConnection() as HttpURLConnection

                        contentList.add(
                            connection.inputStream.bufferedReader().use { it.readText() })
                    }
                } else {
                    val modifiedUrl = modifyUrl(value as String)
                    val connection = URL(modifiedUrl).openConnection() as HttpURLConnection

                    contentList.add(
                        connection.inputStream.bufferedReader().use { it.readText() })
                }
            }
            contentList
        }
    }

    private fun modifyUrl(bbox: String): String {
        val sb = StringBuilder(baseUrl)
            .append(bbox)
            .append("&ucid=0")
            .append("&maxSize=5")
            .append("&zoom=4")

        return sb.toString()
    }

    suspend fun fetchTextFromUrlByBoundingBox(boundingBox: BoundingBox, zoomLevel: Double): String {
        val sb = StringBuilder(baseUrl)
        sb.append("b=${boundingBox.latSouth}")
            .append("&t=${boundingBox.latNorth}")
            .append("&l=${boundingBox.lonWest}")
            .append("&r=${boundingBox.lonEast}")
            .append("&ucid=0")
            .append("&maxSize=5")
            .append("&zoom=${zoomLevel.toInt()}")
        val modifiedUrl = sb.toString()

        return withContext(Dispatchers.IO) {
            val connection = URL(modifiedUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000

            val content = connection.inputStream.bufferedReader().use { it.readText() }

            content
        }
    }
}