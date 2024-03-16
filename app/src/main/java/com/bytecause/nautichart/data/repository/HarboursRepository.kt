package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.tables.HarboursEntity
import com.bytecause.nautichart.data.remote.HarboursRemoteDataSource
import com.bytecause.nautichart.domain.model.ApiResult
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class HarboursRepository @Inject constructor(
    private val remoteDataSource: HarboursRemoteDataSource
) {

    suspend fun getHarbours(
        boundingBox: BoundingBox? = null,
        zoomLevel: Double? = null
    ): ApiResult<List<HarboursEntity>> {
        return try {
            val listOfExtractedValues = ArrayList<HarboursEntity>()
            val fetchType =
                if (boundingBox == null || zoomLevel == null) remoteDataSource.fetchTextFromUrls()
                    .joinToString()
                    .trimIndent() else remoteDataSource.fetchTextFromUrlByBoundingBox(
                    boundingBox,
                    zoomLevel
                ).trimIndent()
            fetchType.let {
                val regex =
                    Regex("""putHarbourMarker\((\d+), (\d+\.\d+), (\d+\.\d+), '(.*?)', '(.*?)', (-?\d+)\);""")
                val regex2 =
                    Regex("""putHarbourMarker\((\d+),\s*(-?\d+\.\d+),\s*(-?\d+\.\d+),\s*'([^']+)',\s*'([^']*)',\s*(-?\d+)\);""")

                if (it.isNotEmpty()) {
                    if (regex.containsMatchIn(it)) {
                        regex.findAll(it).forEach { matchResult ->
                            val harborId = matchResult.groupValues[1].toInt()
                            val longitude = matchResult.groupValues[2].toDouble()
                            val latitude = matchResult.groupValues[3].toDouble()
                            val harborName = matchResult.groupValues[4]
                            val url = matchResult.groupValues[5]
                            val type = matchResult.groupValues[6]

                            val extractedValues =
                                HarboursEntity(harborId, latitude, longitude, harborName, url, type)
                            listOfExtractedValues.add(extractedValues)
                        }
                    }
                    if (regex2.containsMatchIn(it)) {
                        regex2.findAll(it).forEach { matchResult ->
                            val harborId = matchResult.groupValues[1].toInt()
                            val longitude = matchResult.groupValues[2].toDouble()
                            val latitude = matchResult.groupValues[3].toDouble()
                            val harborName = matchResult.groupValues[4]
                            val url = matchResult.groupValues[5]
                            val type = matchResult.groupValues[6]

                            val extractedValues =
                                HarboursEntity(harborId, latitude, longitude, harborName, url, type)
                            listOfExtractedValues.add(extractedValues)
                        }
                    }
                }
            }
            ApiResult.Success(listOfExtractedValues)

        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Failure(e)
        }
    }
}