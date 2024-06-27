package com.bytecause.map.data.repository

import android.util.Log
import com.bytecause.domain.abstractions.VesselsPositionsRemoteRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.map.data.remote.VesselsPositionsRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class VesselsRemoteRepositoryImpl
@Inject
constructor(
    private val remoteDataSource: VesselsPositionsRemoteDataSource,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VesselsPositionsRemoteRepository {
    override suspend fun parseXml(): ApiResult<List<VesselInfoModel>> {
        return withContext(coroutineDispatcher) {
            try {
                val fetchedData = remoteDataSource.fetchTextFromUrl()
                val document: Document = Jsoup.parse(fetchedData)
                val positions = mutableListOf<VesselInfoModel>()

                val vesselElements: List<Element> = document.select("V_POS")
                measureTimeMillis {
                    for (vesselElement in vesselElements) {
                        val position =
                            VesselInfoModel(
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
                                timeStamp = System.currentTimeMillis(),
                            )
                        positions.add(position)
                    }
                }
                ApiResult.Success(data = positions)
            } catch (e: IOException) {
                e.printStackTrace()
                ApiResult.Failure(exception = e)
            }
        }
    }
}
