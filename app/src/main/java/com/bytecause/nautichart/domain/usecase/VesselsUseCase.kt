package com.bytecause.nautichart.domain.usecase

import android.util.Log
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import com.bytecause.nautichart.data.repository.VesselsDatabaseRepository
import com.bytecause.nautichart.data.repository.VesselsPositionsRepository
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class VesselsUseCase @Inject constructor(
    private val vesselsDatabaseRepository: VesselsDatabaseRepository,
    private val vesselsPositionsRepository: VesselsPositionsRepository
) {

    fun searchVesselById(id: Int): Flow<VesselInfoEntity> = vesselsDatabaseRepository.searchVesselById(id)

    fun fetchVessels(): Flow<ApiResult<List<VesselInfoEntity>>> {
        return flow {
            vesselsDatabaseRepository.loadAllVessels.firstOrNull()?.let { cachedVessels ->
                if (cachedVessels.isEmpty()) {
                    vesselsPositionsRepository.parseXml().let { result ->
                        if (result.exception == null && result.data != null) {
                            vesselsDatabaseRepository.addAllVessels(result.data)
                            emit(result)
                        } else {
                            Log.e(TAG(this), "exception")
                            emit(result)
                        }
                    }
                }
                else if (vesselsDatabaseRepository.shouldUpdateVesselDatabase(System.currentTimeMillis())
                        .firstOrNull() == true
                ) {
                    vesselsDatabaseRepository.deleteAllVessels()
                    vesselsPositionsRepository.parseXml().let { result ->
                        if (result.exception == null && result.data != null) {
                            vesselsDatabaseRepository.addAllVessels(result.data)
                            emit(result)
                        } else {
                            emit(result)
                        }
                    }
                } else {
                    emit(ApiResult.Success(data = cachedVessels))
                }
            }
        }.flowOn(Dispatchers.Default)
    }

}