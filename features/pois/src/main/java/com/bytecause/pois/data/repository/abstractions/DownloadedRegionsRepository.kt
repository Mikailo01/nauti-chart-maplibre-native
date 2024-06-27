package com.bytecause.pois.data.repository.abstractions

import kotlinx.coroutines.flow.Flow


interface DownloadedRegionsRepository {

    fun getDownloadedRegionsIds(): Flow<Set<String>>
    suspend fun addDownloadedRegion(regionId: String)
}