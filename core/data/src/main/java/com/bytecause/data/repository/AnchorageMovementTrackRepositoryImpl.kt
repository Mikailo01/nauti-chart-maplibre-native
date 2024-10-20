package com.bytecause.data.repository

import com.bytecause.data.local.room.dao.AnchorageMovementTrackDao
import com.bytecause.data.mappers.asAnchorageMovementTrackEntity
import com.bytecause.data.mappers.asAnchorageMovementTrackModel
import com.bytecause.data.model.AnchorageMovementTrackModel
import com.bytecause.data.repository.abstractions.AnchorageMovementTrackRepository
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnchorageMovementTrackRepositoryImpl @Inject constructor(
    private val dao: AnchorageMovementTrackDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnchorageMovementTrackRepository {
    override suspend fun insertPosition(position: AnchorageMovementTrackModel) {
        withContext(coroutineDispatcher) {
            dao.insertPosition(position.asAnchorageMovementTrackEntity())
        }
    }

    override suspend fun clear() {
        withContext(coroutineDispatcher) {
            dao.clear()
        }
    }

    override fun getTracks(): Flow<List<AnchorageMovementTrackModel>> = dao.getTracks()
        .map { originalList -> mapList(originalList) { it.asAnchorageMovementTrackModel() } }
        .flowOn(coroutineDispatcher)
}