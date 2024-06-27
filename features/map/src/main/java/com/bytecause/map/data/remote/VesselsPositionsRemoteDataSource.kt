package com.bytecause.map.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


class VesselsPositionsRemoteDataSource {

    private val url =
        URL("https://www.marinetraffic.com/legacy/getxml_i?sw_x=-180&sw_y=-90&ne_x=180&ne_y=90&zoom=8")

    suspend fun fetchTextFromUrl(): String = withContext(Dispatchers.IO) {
        url.openStream().use {
            it.reader().readText()
        }
    }
}