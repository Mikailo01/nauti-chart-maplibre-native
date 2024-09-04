package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.HarboursMetadataDatasetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarboursMetadataDatasetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: HarboursMetadataDatasetEntity)

    @Query("DELETE FROM harbours_metadata_dataset")
    suspend fun deleteDataset()

    @Query("SELECT * FROM harbours_metadata_dataset")
    fun getDataset(): Flow<HarboursMetadataDatasetEntity?>
}