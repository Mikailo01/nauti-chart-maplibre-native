package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.VesselsMetadataDatasetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VesselsMetadataDatasetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: VesselsMetadataDatasetEntity)

    @Query("DELETE FROM vessels_metadata_dataset")
    suspend fun deleteDataset()

    @Query("SELECT * FROM vessels_metadata_dataset")
    fun getDataset(): Flow<VesselsMetadataDatasetEntity?>
}