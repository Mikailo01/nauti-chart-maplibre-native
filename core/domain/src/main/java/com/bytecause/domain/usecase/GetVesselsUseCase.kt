package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.VesselModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class GetVesselsUseCase(
    private val vesselsDatabaseRepository: VesselsDatabaseRepository,
    private val updateVesselsUseCase: UpdateVesselsUseCase
) {
    operator fun invoke(): Flow<ApiResult<List<VesselModel>>> = flow {
        // Ensure the vessel positions are present and up-to-date
        val updateResult = updateVesselsUseCase().firstOrNull()
        if (updateResult is ApiResult.Failure) {
            emit(ApiResult.Failure(
                exception = updateResult.exception
                    ?: IllegalStateException("Unknown error occurred")
            ))  // Emit failure if updating the vessels fails
            return@flow
        }

        // Fetch vessels from the database
        val vessels = vesselsDatabaseRepository.loadAllVessels().firstOrNull()
        emit(ApiResult.Success(data = vessels))
    }
}
