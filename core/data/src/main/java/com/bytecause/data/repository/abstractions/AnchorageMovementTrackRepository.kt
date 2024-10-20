package com.bytecause.data.repository.abstractions

import com.bytecause.data.model.AnchorageMovementTrackModel
import kotlinx.coroutines.flow.Flow

interface AnchorageMovementTrackRepository {
    suspend fun insertPosition(position: AnchorageMovementTrackModel)
    suspend fun clear()
    fun getTracks(): Flow<List<AnchorageMovementTrackModel>>
}