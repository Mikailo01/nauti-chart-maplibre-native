package com.bytecause.data.repository.abstractions

interface SearchManager {
    suspend fun openSession()
    suspend fun putResults(results: List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>): Boolean
    suspend fun searchCachedResult(query: String): List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>
    fun closeSession()
}