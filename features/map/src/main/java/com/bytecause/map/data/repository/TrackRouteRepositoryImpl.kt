package com.bytecause.map.data.repository

import com.bytecause.data.local.room.dao.TrackRouteDao
import com.bytecause.data.mappers.asRouteRecordEntity
import com.bytecause.data.mappers.asRouteRecordModel
import com.bytecause.domain.abstractions.TrackRouteRepository
import com.bytecause.domain.model.RouteRecordModel
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackRouteRepositoryImpl @Inject constructor(
    private val trackRouteDao: TrackRouteDao,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : TrackRouteRepository {

    override suspend fun saveRecord(record: RouteRecordModel) {
        withContext(coroutineContext) {
            trackRouteDao.saveRecord(record.asRouteRecordEntity())
        }
    }

    override suspend fun removeRecord(id: Long) {
        withContext(coroutineContext) {
            trackRouteDao.removeRecord(id)
        }
    }

    override fun getRecordById(id: Long): Flow<RouteRecordModel> = trackRouteDao.getRecordById(id)
        .map { it.asRouteRecordModel() }
        .flowOn(coroutineContext)

    override fun getRecordByTimestamp(timestamp: Long): Flow<RouteRecordModel> =
        trackRouteDao.getRecordByTimestamp(timestamp)
            .map { it.asRouteRecordModel() }
            .flowOn(coroutineContext)

    override fun getRecords(): Flow<List<RouteRecordModel>> = trackRouteDao.getRecords()
        .map { originalList -> mapList(originalList) { it.asRouteRecordModel() } }
        .flowOn(coroutineContext)
}