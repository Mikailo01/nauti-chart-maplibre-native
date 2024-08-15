package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.abstractions.VesselsPositionsRemoteRepository
import com.bytecause.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class UpdateVesselsUseCase(
    private val vesselsDatabaseRepository: VesselsDatabaseRepository,
    private val vesselsPositionsRemoteRepository: VesselsPositionsRemoteRepository,
) {
    operator fun invoke(): Flow<ApiResult<Unit>> = flow {
        when {
            shouldUpdateVessels() -> {
                vesselsDatabaseRepository.deleteAllVessels()
                val result = updateVesselsFromRemote()
                emit(result)
            }

            vesselsDatabaseRepository.isVesselDatabaseEmpty().firstOrNull() == true -> {
                val result = updateVesselsFromRemote()
                emit(result)
            }

            else -> emit(ApiResult.Success(Unit))
        }
    }

    private suspend fun shouldUpdateVessels(): Boolean {
        return vesselsDatabaseRepository.shouldUpdateVesselDatabase(System.currentTimeMillis())
            .firstOrNull() == true
    }

    private suspend fun updateVesselsFromRemote(): ApiResult<Unit> {
        return vesselsPositionsRemoteRepository.parseXml().let { result ->
            when {
                result.exception == null && result.data != null -> {
                    vesselsDatabaseRepository.addAllVessels(result.data)
                    ApiResult.Success(Unit)
                }

                else -> {
                    ApiResult.Failure(
                        exception = result.exception
                            ?: IllegalStateException("Unknown error occurred")
                    )
                }
            }
        }
    }
}

