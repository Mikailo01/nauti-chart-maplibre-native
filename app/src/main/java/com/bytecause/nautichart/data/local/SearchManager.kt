package com.bytecause.nautichart.data.local

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SearchSpec.RANKING_STRATEGY_RELEVANCE_SCORE
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.bytecause.nautichart.data.local.room.tables.SearchPlaceCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchManager(
    private val applicationContext: Context
) {
    private var session: AppSearchSession? = null

    suspend fun openSession() {
        withContext(Dispatchers.IO) {
            val sessionFuture = LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(
                    applicationContext,
                    "search_cache",
                ).build()
            )
            val setSchemaRequest = SetSchemaRequest.Builder()
                .addDocumentClasses(SearchPlaceCacheEntity::class.java)
                .build()

            session = sessionFuture.get()
            session?.setSchemaAsync(setSchemaRequest)
        }
    }

    suspend fun putResults(results: List<SearchPlaceCacheEntity>): Boolean {
        return withContext(Dispatchers.IO) {
            session?.putAsync(
                PutDocumentsRequest.Builder()
                    .addDocuments(results)
                    .build()
            )?.get()?.isSuccess == true
        }
    }

    suspend fun searchCachedResult(query: String): List<SearchPlaceCacheEntity> {
        return withContext(Dispatchers.IO) {
            val searchSpec = SearchSpec.Builder()
                .setRankingStrategy(RANKING_STRATEGY_RELEVANCE_SCORE)
                .build()
            val result = session?.search(
                query,
                searchSpec
            ) ?: return@withContext emptyList()

            val page = result.nextPageAsync.get()

            page.mapNotNull {
                if (it.genericDocument.schemaType == SearchPlaceCacheEntity::class.java.simpleName) {
                    it.getDocument(SearchPlaceCacheEntity::class.java)
                } else null
            }
        }
    }

    fun closeSession() {
        session?.close()
        session = null
    }
}