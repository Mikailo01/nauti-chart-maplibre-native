package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.VesselInfoDao
import com.bytecause.nautichart.data.local.room.tables.VesselInfoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VesselsDatabaseRepository @Inject constructor(
    private val vesselInfoDao: VesselInfoDao
) {

    val loadAllVessels: Flow<List<VesselInfoEntity>> = vesselInfoDao.loadAllVessels()
    val isVesselDatabaseEmpty: Flow<Boolean> = vesselInfoDao.isVesselDatabaseEmpty()
    fun searchVesselById(id: Int): Flow<VesselInfoEntity> = vesselInfoDao.searchVesselById(id)

    fun shouldUpdateVesselDatabase(currentTimeMillis: Long): Flow<Boolean> =
        vesselInfoDao.shouldUpdateVesselDatabase(currentTimeMillis)

    suspend fun deleteAllVessels() {
        vesselInfoDao.deleteAllVessels()
    }

    suspend fun addAllVessels(vesselInfo: List<VesselInfoEntity>) {
        vesselInfoDao.addAllVessels(vesselInfo)
    }
}