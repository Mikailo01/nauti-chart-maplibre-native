package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.abstractions.OverpassRepository
import com.bytecause.domain.abstractions.makeQuery
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.HarboursModel
import com.bytecause.domain.model.OverpassNodeModel
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.domain.util.SearchTypes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UpdateHarboursUseCase(
    private val harboursDatabaseRepository: HarboursDatabaseRepository,
    private val overpassRepository: OverpassRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(): Flow<ApiResult<Unit>> = flow {
        val isEmpty = harboursDatabaseRepository.isHarboursDatabaseEmpty().firstOrNull() ?: true

        if (isEmpty) {
            val query: String = OverpassQueryBuilder
                .format(OverpassQueryBuilder.FormatTypes.JSON)
                .timeout(120)
                .wholeWorld()
                .search(
                    OverpassQueryBuilder.Type.Node,
                    SearchTypes.UnionSet(
                        listOf(
                            "seamark:type",
                            "leisure"
                        )
                    ).filter(listOf("harbour"), listOf("marina"))
                )
                .build()

            overpassRepository.makeQuery<OverpassNodeModel>(query).also { result ->
                when {
                    result.exception != null -> {
                        emit(ApiResult.Failure(exception = result.exception))
                        return@flow
                    }

                    !result.data.isNullOrEmpty() -> {
                        result.data.map {
                            HarboursModel(
                                latitude = it.lat,
                                longitude = it.lon,
                                tags = it.tags
                            )
                        }.let {
                            harboursDatabaseRepository.insertAllHarbours(it)
                            emit(ApiResult.Success(Unit))
                        }
                    }
                }
            }
        } else {
            // harbours present, emit success
            emit(ApiResult.Success(Unit))
        }
    }
        .flowOn(coroutineDispatcher)
}