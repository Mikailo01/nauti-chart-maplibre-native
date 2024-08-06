package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.HarboursEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarboursDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHarbours(harbours: List<HarboursEntity>)

    @Query("SELECT * FROM harbours")
    fun loadAllHarbours(): Flow<List<HarboursEntity>>

    @Query("SELECT * FROM harbours WHERE id = :id")
    fun searchHarbourById(id: Int): Flow<HarboursEntity>

    @Query("SELECT (SELECT COUNT(*) FROM harbours) == 0")
    fun isHarbourDatabaseEmpty(): Flow<Boolean>

    @Query("SELECT (SELECT COUNT(*) FROM harbours WHERE id IN (:idList)) >= 1")
    fun isHarbourIdInDatabase(idList: List<Int>): Flow<Boolean>
}