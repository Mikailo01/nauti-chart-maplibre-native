package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.RadiusPoiMetadataDatasetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RadiusPoiMetadataDatasetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: RadiusPoiMetadataDatasetEntity): Long

    @Query("DELETE FROM radius_poi_metadata_dataset WHERE category = :categoryName")
    suspend fun deleteDatasetByName(categoryName: String)

    @Query("SELECT * FROM radius_poi_metadata_dataset WHERE category = :categoryName")
    fun getDatasetByName(categoryName: String): Flow<RadiusPoiMetadataDatasetEntity?>
}