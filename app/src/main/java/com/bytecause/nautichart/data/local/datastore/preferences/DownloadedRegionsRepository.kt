package com.bytecause.nautichart.data.local.datastore.preferences

import kotlinx.coroutines.flow.Flow


interface DownloadedRegionsRepository {

    fun getDownloadedRegionsIds(): Flow<Set<String>>
    suspend fun addDownloadedRegion(regionId: String)
}