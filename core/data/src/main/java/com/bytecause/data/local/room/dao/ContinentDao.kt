package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.bytecause.data.local.room.tables.ContinentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContinentDao {

    @Query("SELECT * FROM continent")
    fun getAllContinents(): Flow<List<ContinentEntity>>
}