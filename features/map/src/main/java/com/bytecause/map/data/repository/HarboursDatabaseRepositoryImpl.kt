package com.bytecause.map.data.repository

import com.bytecause.data.di.IoDispatcher
import com.bytecause.data.local.room.dao.HarboursDao
import com.bytecause.data.mappers.asHarboursEntity
import com.bytecause.data.mappers.asHarboursModel
import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.model.HarboursModel
import com.bytecause.util.mappers.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HarboursDatabaseRepositoryImpl @Inject constructor(
    private val harboursDao: HarboursDao,
    @IoDispatcher
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HarboursDatabaseRepository {
    override fun loadAllHarbours(): Flow<List<HarboursModel>> =
        harboursDao.loadAllHarbours()
            .map { originalList -> mapList(originalList) { it.asHarboursModel() } }
            .flowOn(coroutineDispatcher)

    override fun isHarboursDatabaseEmpty(): Flow<Boolean> = harboursDao.isHarbourDatabaseEmpty()
        .flowOn(coroutineDispatcher)

    override fun isHarbourIdInDatabase(idList: List<Int>): Flow<Boolean> =
        harboursDao.isHarbourIdInDatabase(idList)
            .flowOn(coroutineDispatcher)

    override suspend fun insertAllHarbours(harbours: List<HarboursModel>) {
        withContext(coroutineDispatcher) {
            harboursDao.insertAllHarbours(mapList(harbours) { it.asHarboursEntity() })
        }
    }

    override fun searchHarbourById(id: Int): Flow<HarboursModel> =
        harboursDao.searchHarbourById(id)
            .map { it.asHarboursModel() }
            .flowOn(coroutineDispatcher)
}