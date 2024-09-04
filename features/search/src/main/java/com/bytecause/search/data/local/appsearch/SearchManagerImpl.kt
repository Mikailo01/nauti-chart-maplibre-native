package com.bytecause.search.data.local.appsearch

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SearchSpec.RANKING_STRATEGY_RELEVANCE_SCORE
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.bytecause.data.repository.abstractions.SearchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchManagerImpl(
    private val applicationContext: Context,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchManager {
    private var session: AppSearchSession? = null

    override suspend fun openSession() {
        withContext(coroutineDispatcher) {
            val sessionFuture = LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(
                    applicationContext,
                    "search_cache",
                ).build()
            )
            val setSchemaRequest = SetSchemaRequest.Builder()
                .addDocumentClasses(com.bytecause.data.local.room.tables.SearchPlaceCacheEntity::class.java)
                .build()

            session = sessionFuture.get()
            session?.setSchemaAsync(setSchemaRequest)
        }
    }

    override suspend fun putResults(results: List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity>): Boolean {
        return withContext(coroutineDispatcher) {
            session?.putAsync(
                PutDocumentsRequest.Builder()
                    .addDocuments(results)
                    .build()
            )?.get()?.isSuccess == true
        }
    }

    override suspend fun searchCachedResult(query: String): List<com.bytecause.data.local.room.tables.SearchPlaceCacheEntity> {
        return withContext(coroutineDispatcher) {
            val searchSpec = SearchSpec.Builder()
                .setRankingStrategy(RANKING_STRATEGY_RELEVANCE_SCORE)
                .build()
            val result = session?.search(
                query,
                searchSpec
            ) ?: return@withContext emptyList()

            val page = result.nextPageAsync.get()

            page.mapNotNull {
                if (it.genericDocument.schemaType == com.bytecause.data.local.room.tables.SearchPlaceCacheEntity::class.java.simpleName) {
                    it.getDocument(com.bytecause.data.local.room.tables.SearchPlaceCacheEntity::class.java)
                } else null
            }
        }
    }

    override fun closeSession() {
        session?.close()
        session = null
    }
}