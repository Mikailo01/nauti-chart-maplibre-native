package com.bytecause.nautichart.data.repository

import com.bytecause.nautichart.data.local.room.HarboursDao
import com.bytecause.nautichart.data.local.room.tables.HarboursEntity
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