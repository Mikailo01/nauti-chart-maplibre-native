package com.bytecause.map.data.repository

import com.bytecause.data.local.room.dao.VesselInfoDao
import com.bytecause.data.mappers.asVesselInfoEntity
import com.bytecause.data.mappers.asVesselInfoModel
import com.bytecause.data.mappers.asVesselModel
import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VesselsDatabaseRepositoryImpl
@Inject
constructor(
    private val vesselInfoDao: VesselInfoDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VesselsDatabaseRepository {
    override fun loadAllVessels(): Flow<List<VesselModel>> =
        vesselInfoDao.loadAllVessels()
            .map { originalList -> mapList(originalList) { it.asVesselModel() } }
            .flowOn(coroutineDispatcher)

    override fun isVesselDatabaseEmpty(): Flow<Boolean> = vesselInfoDao.isVesselDatabaseEmpty()
        .flowOn(coroutineDispatcher)

    override fun searchVesselById(id: Int): Flow<VesselInfoModel> =
        vesselInfoDao.searchVesselById(id)
            .map { it.asVesselInfoModel() }
            .flowOn(coroutineDispatcher)

    override fun shouldUpdateVesselDatabase(currentTimeMillis: Long): Flow<Boolean> =
        vesselInfoDao.shouldUpdateVesselDatabase(currentTimeMillis)
            .flowOn(coroutineDispatcher)

    override suspend fun addAllVessels(vesselInfo: List<VesselInfoModel>) {
        withContext(coroutineDispatcher) {
            vesselInfoDao.addAllVessels(mapList(vesselInfo) { it.asVesselInfoEntity() })
        }
    }
}
