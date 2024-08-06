package com.bytecause.domain.usecase

import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.abstractions.VesselsPositionsRemoteRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.VesselModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class FetchVesselsUseCase(
    private val vesselsDatabaseRepository: VesselsDatabaseRepository,
    private val vesselsPositionsRemoteRepository: VesselsPositionsRemoteRepository,
) {
    operator fun invoke(): Flow<ApiResult<List<VesselModel>>> =
        vesselsDatabaseRepository.loadAllVessels().map {
            val result = when {
                it.isEmpty() -> updateVesselsFromRemote()
                shouldUpdateVessels() -> {
                    vesselsDatabaseRepository.deleteAllVessels()
                    updateVesselsFromRemote()
                }

                else -> ApiResult.Success(data = it)
            }
            result
        }

    private suspend fun shouldUpdateVessels(): Boolean {
        return vesselsDatabaseRepository.shouldUpdateVesselDatabase(System.currentTimeMillis())
            .firstOrNull() == true
    }

    private suspend fun updateVesselsFromRemote(): ApiResult<List<VesselModel>> {
        return vesselsPositionsRemoteRepository.parseXml().let { result ->
            when {
                result.exception == null && result.data != null -> {
                    vesselsDatabaseRepository.addAllVessels(result.data)
                    val updatedVessels = vesselsDatabaseRepository.loadAllVessels().firstOrNull()
                    ApiResult.Success(data = updatedVessels)
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
