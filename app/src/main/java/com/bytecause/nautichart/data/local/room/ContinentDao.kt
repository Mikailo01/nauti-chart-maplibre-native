package com.bytecause.nautichart.data.local.room

import androidx.room.Dao
import androidx.room.Query
import com.bytecause.nautichart.data.local.room.tables.Continent
import kotlinx.coroutines.flow.Flow

@Dao
interface ContinentDao {

    @Query("SELECT * FROM continent")
    fun getAllContinents(): Flow<List<Continent>>
}