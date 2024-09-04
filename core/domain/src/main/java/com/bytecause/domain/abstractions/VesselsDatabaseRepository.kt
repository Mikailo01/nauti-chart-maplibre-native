package com.bytecause.domain.abstractions

import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import kotlinx.coroutines.flow.Flow

interface VesselsDatabaseRepository {
    fun loadAllVessels(): Flow<List<VesselModel>>
    fun isVesselDatabaseEmpty(): Flow<Boolean>
    fun searchVesselById(id: Int): Flow<VesselInfoModel>
    fun shouldUpdateVesselDatabase(currentTimeMillis: Long): Flow<Boolean>
    suspend fun addAllVessels(vesselInfo: List<VesselInfoModel>)
}