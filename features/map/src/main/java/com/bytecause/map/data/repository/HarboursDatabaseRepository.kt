package com.bytecause.map.data.repository

import com.bytecause.data.local.room.dao.HarboursDao
import com.bytecause.data.local.room.tables.HarboursEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HarboursDatabaseRepository @Inject constructor(
    private val harboursDao: HarboursDao
) {

    val loadAllHarbours: Flow<List<HarboursEntity>> = harboursDao.loadAllHarbours()
    val isHarboursDatabaseEmpty: Flow<Boolean> = harboursDao.isHarbourDatabaseEmpty()

    fun isHarbourIdInDatabase(idList: List<Int>) = harboursDao.isHarbourIdInDatabase(idList)

    fun searchHarbourById(id: Int): Flow<HarboursEntity> = harboursDao.searchHarbourById(id)

    suspend fun insertAllHarbours(harbours: List<HarboursEntity>) {
        harboursDao.insertAllHarbours(harbours)
    }
}