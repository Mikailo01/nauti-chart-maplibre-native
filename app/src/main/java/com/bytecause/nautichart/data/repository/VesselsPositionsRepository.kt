package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import com.bytecause.nautichart.data.remote.VesselsPositionsRemoteDataSource
import com.bytecause.nautichart.domain.model.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class VesselsPositionsRepository @Inject constructor(
    private val remoteDataSource: VesselsPositionsRemoteDataSource,
) {

    suspend fun parseXml(): ApiResult<List<VesselInfoEntity>> {
        return try {
            val fetchedData = remoteDataSource.fetchTextFromUrl()
            withContext(Dispatchers.Default) {
                    val document: Document = Jsoup.parse(fetchedData)
                    val positions = mutableListOf<VesselInfoEntity>()

                    val vesselElements: List<Element> = document.select("V_POS")
                    for (vesselElement in vesselElements) {
                        val position = VesselInfoEntity(
                            latitude = vesselElement.attr("LAT"),
                            longitude = vesselElement.attr("LON"),
                            name = vesselElement.attr("N"),
                            type = vesselElement.attr("T"),
                            heading = vesselElement.attr("H"),
                            speed = vesselElement.attr("S"),
                            flag = vesselElement.attr("F"),
                            mmsi = vesselElement.attr("M"),
                            length = vesselElement.attr("L"),
                            eta = vesselElement.attr("E"),
                            timeStamp = System.currentTimeMillis()
                        )
                        positions.add(position)
                    }
                    ApiResult.Success(data = positions)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ApiResult.Failure(exception = e)
        }
    }
}