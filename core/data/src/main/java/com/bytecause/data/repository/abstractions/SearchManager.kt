package com.bytecause.data.repository.abstractions

import com.bytecause.data.local.room.tables.SearchPlaceCacheEntity
import kotlinx.coroutines.flow.Flow

interface SearchManager {
    suspend fun openSession()
    suspend fun putResults(results: List<SearchPlaceCacheEntity>): Boolean
    fun searchCachedResult(query: String): Flow<List<SearchPlaceCacheEntity>>
    fun closeSession()
}