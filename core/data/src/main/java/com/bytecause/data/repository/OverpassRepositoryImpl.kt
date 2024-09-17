package com.bytecause.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.remote.retrofit.OverpassRestApiService
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassElement
import com.bytecause.domain.model.OverpassElementTypeAdapter
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.reflect.KClass

private const val BATCH_SIZE = 1000

class OverpassRepositoryImpl @Inject constructor(
    private val overpassRestApiService: OverpassRestApiService,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OverpassRepository {

    // Overpass API returns different JSON scheme for Nodes and Relations, so we have to pass correct
    // type argument.
    // To avoid OutOfMemoryException I introduced incremental parsing, so each 1000 parsed objects
    // are emitted, collected and cached in the database. It is slower but safer.
    @Suppress("UNCHECKED_CAST")
    override fun <T : OverpassElement> makeQuery(
        query: String,
        clazz: KClass<T>,
        getTimestamp: Boolean
    ): Flow<ApiResult<Pair<String?, List<T>>>> = flow<ApiResult<Pair<String?, List<T>>>> {
        val response = overpassRestApiService.makeQuery(query)

        if (response.isSuccessful) {
            // batch used for storing up-to 1000 elements
            val batch = mutableListOf<T>()

            response.body()?.charStream().use {
                val reader = JsonReader(it)

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "osm3s" -> {
                            if (getTimestamp) {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    if (reader.nextName() == "timestamp_osm_base") {
                                        // get the timestamp of the last update of the dataset
                                        val timestamp = reader.nextString()
                                        emit(ApiResult.Success(timestamp to emptyList()))
                                    } else reader.skipValue()
                                }
                                reader.endObject()
                            } else reader.skipValue()
                        }

                        "elements" -> {
                            reader.beginArray()

                            // Handle empty response
                            if (!reader.hasNext()) {
                                emit(ApiResult.Failure(exception = NoSuchElementException()))
                            }

                            while (reader.hasNext()) {
                                val element = OverpassElementTypeAdapter().read(reader)
                                if (element != null && clazz.isInstance(element)) {
                                    batch.add(element as T)
                                }

                                if (batch.size >= BATCH_SIZE) {
                                    emit(ApiResult.Success(null to batch.toList()))
                                    batch.clear()  // Clear after emission to handle the next batch
                                }
                            }
                            reader.endArray()
                        }

                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                // Emit any remaining elements
                if (batch.isNotEmpty()) {
                    emit(ApiResult.Success(null to batch.toList()))
                }
            }
        } else {
            emit(ApiResult.Failure(exception = Exception("${response.code()}")))
        }
    }
        .catch { e ->
            e.printStackTrace()
            emit(ApiResult.Failure(exception = e))
        }
        .flowOn(coroutineDispatcher)
}