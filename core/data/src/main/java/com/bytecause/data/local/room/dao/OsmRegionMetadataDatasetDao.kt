package com.bytecause.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytecause.data.local.room.tables.OsmRegionMetadataDatasetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OsmRegionMetadataDatasetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: OsmRegionMetadataDatasetEntity): Long

    @Query("DELETE FROM osm_region_metadata_dataset WHERE id = :regionId")
    suspend fun deleteDatasetById(regionId: Int)

    @Query("SELECT * FROM osm_region_metadata_dataset WHERE id = :regionId")
    fun getDatasetById(regionId: Int): Flow<OsmRegionMetadataDatasetEntity?>

    @Query("SELECT * FROM osm_region_metadata_dataset")
    fun getAllDatasets(): Flow<List<OsmRegionMetadataDatasetEntity>>
}
