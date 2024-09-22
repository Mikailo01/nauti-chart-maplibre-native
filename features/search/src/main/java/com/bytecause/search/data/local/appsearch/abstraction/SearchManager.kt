package com.bytecause.search.data.local.appsearch.abstraction

import kotlinx.coroutines.flow.Flow

interface SearchManager {
    suspend fun openSession()
    suspend fun putResults(results: List<com.bytecause.search.data.local.appsearch.SearchPlaceCacheEntity>): Boolean
    fun searchCachedResult(query: String): Flow<List<com.bytecause.search.data.local.appsearch.SearchPlaceCacheEntity>>
    fun closeSession()
}