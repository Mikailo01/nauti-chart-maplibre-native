package com.bytecause.domain.model

sealed interface DatasetType {
   data object Harbours : DatasetType
    data class RegionPoi(val regionName: String) : DatasetType
}

sealed interface UpdateResult {
    data class UpdateStarted(val type: DatasetType) : UpdateResult
    data class UpdateProgress(val progress: Int) : UpdateResult
    data class UpdateFailed(val error: Throwable) : UpdateResult
    data object DatasetUpdateFinished : UpdateResult
    data object DatasetsUpToDate : UpdateResult
}