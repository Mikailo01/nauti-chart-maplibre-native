package com.bytecause.domain.abstractions

import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassElement
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass


/*
We cannot define inline method in interface, so to be able to preserve object type after type erasure,
this extension function takes query string along with receiver's object type and pass it to makeQuery method.
*/
/**
 * @return [Flow] with [ApiResult] which holds [Pair] where [Pair.first] contains timestamp of last dataset
 * update and [Pair.second] contains dataset batch returned by the [OverpassRepository].
 */
inline fun <reified T : OverpassElement> OverpassRepository.makeQuery(
    query: String,
    getTimestamp: Boolean = true
) =
    makeQuery(query, T::class, getTimestamp = getTimestamp)

interface OverpassRepository {
    fun <T : OverpassElement> makeQuery(
        query: String,
        clazz: KClass<T>,
        getTimestamp: Boolean
    ): Flow<ApiResult<Pair<String?, List<T>>>>
}