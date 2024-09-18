package com.bytecause.search.data.local.appsearch

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SearchSpec.RANKING_STRATEGY_RELEVANCE_SCORE
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.bytecause.data.local.room.tables.SearchPlaceCacheEntity
import com.bytecause.data.repository.abstractions.SearchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext


internal const val RECENTLY_SEARCHED_PLACE_NAMESPACE = "recently_searched_place"

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
                .addDocumentClasses(SearchPlaceCacheEntity::class.java)
                .build()

            session = sessionFuture.get()
            session?.setSchemaAsync(setSchemaRequest)
        }
    }

    override suspend fun putResults(results: List<SearchPlaceCacheEntity>): Boolean {
        return withContext(coroutineDispatcher) {
            session?.putAsync(
                PutDocumentsRequest.Builder()
                    .addDocuments(results)
                    .build()
            )?.get()?.isSuccess == true
        }
    }

    override fun searchCachedResult(query: String): Flow<List<SearchPlaceCacheEntity>> =
        flow {
            val searchSpec = SearchSpec.Builder()
                .setRankingStrategy(RANKING_STRATEGY_RELEVANCE_SCORE)
                .build()

            val result = session?.search(
                query,
                searchSpec
            ) ?: run {
                emit(emptyList())
                return@flow
            }

            val page = result.nextPageAsync.get()

            emit(page.mapNotNull {
                if (it.genericDocument.schemaType == SearchPlaceCacheEntity::class.java.simpleName) {
                    it.getDocument(SearchPlaceCacheEntity::class.java)
                } else null
            }
            )
        }
            .flowOn(coroutineDispatcher)

    override fun closeSession() {
        session?.close()
        session = null
    }
}