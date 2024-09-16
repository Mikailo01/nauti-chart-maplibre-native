package com.bytecause.data.repository.abstractions

import kotlinx.coroutines.flow.Flow

interface SearchManager {
    suspend fun openSession()
    suspend fun putResults(results: List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>): Boolean
    fun searchCachedResult(query: String): Flow<List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>>
    fun closeSession()
}