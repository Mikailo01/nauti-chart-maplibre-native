package com.bytecause.map.data.repository

import com.bytecause.data.local.room.dao.TrackRouteDao
import com.bytecause.data.local.room.tables.RouteRecordEntity
import com.bytecause.map.data.repository.abstraction.TrackRouteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRouteRepositoryImpl @Inject constructor(
    private val trackRouteDao: TrackRouteDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TrackRouteRepository {

    override suspend fun saveRecord(record: RouteRecordEntity) {
        withContext(coroutineDispatcher) {
            trackRouteDao.saveRecord(record)
        }
    }

    override suspend fun removeRecord(id: Long) {
        withContext(coroutineDispatcher) {
            trackRouteDao.removeRecord(id)
        }
    }

    override fun getRecordById(id: Long): Flow<RouteRecordEntity> = trackRouteDao.getRecordById(id)
        .flowOn(coroutineDispatcher)

    override fun getRecords(): Flow<List<RouteRecordEntity>> = trackRouteDao.getRecords()
        .flowOn(coroutineDispatcher)
}