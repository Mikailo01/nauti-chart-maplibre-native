package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.HarboursModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class GetHarboursUseCase(
    private val harboursDatabaseRepository: HarboursDatabaseRepository,
    private val fetchHarboursUseCase: UpdateHarboursUseCase
) {

    operator fun invoke(): Flow<ApiResult<List<HarboursModel>>> = flow {
        // Ensure that harbours are present in database
        val updateResult = fetchHarboursUseCase().firstOrNull()
        if (updateResult is ApiResult.Failure) {
            emit(
                ApiResult.Failure(
                    exception = updateResult.exception
                        ?: IllegalStateException("Unknown error occurred")
                )
            )  // Emit failure if updating the vessels fails
            return@flow
        }

        val harbours = harboursDatabaseRepository.loadAllHarbours().firstOrNull()
        emit(ApiResult.Success(data = harbours))
    }
}