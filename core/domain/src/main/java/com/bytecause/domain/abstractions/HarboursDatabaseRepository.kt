package com.bytecause.domain.abstractions

import com.bytecause.domain.model.HarboursModel
import kotlinx.coroutines.flow.Flow

interface HarboursDatabaseRepository {
    fun loadAllHarbours(): Flow<List<HarboursModel>>
    fun isHarboursDatabaseEmpty(): Flow<Boolean>
    fun searchHarbourById(id: Int): Flow<HarboursModel>
    fun isHarbourIdInDatabase(idList: List<Int>): Flow<Boolean>
    suspend fun insertAllHarbours(harbours: List<HarboursModel>)
}