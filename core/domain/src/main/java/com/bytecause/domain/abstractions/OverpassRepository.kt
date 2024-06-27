package com.bytecause.domain.abstractions

import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.OverpassElement
import kotlin.reflect.KClass

/**
 * We cannot define inline method in interface, so to be able to preserve object type after type erasure,
 * this extension function takes query string along with receiver's object type and pass it to makeQuery method.
 * */
suspend inline fun <reified T : OverpassElement> OverpassRepository.makeQuery(query: String) = makeQuery(query, T::class)

interface OverpassRepository {
    suspend fun <T : OverpassElement> makeQuery(query: String, clazz: KClass<T>): ApiResult<List<T>>
}