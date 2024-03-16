package com.bytecause.nautichart.data.local.room.tables

import androidx.appsearch.annotation.Document
import androidx.appsearch.app.AppSearchSchema

@Document
data class SearchPlaceCacheEntity(
    @Document.Namespace val nameSpace: String = "",
    @Document.Id val placeId: String = "",
    @Document.DoubleProperty
    val latitude: Double = 0.0,
    @Document.DoubleProperty
    val longitude: Double = 0.0,
    @Document.StringProperty(
        indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES
    )
    val name: String = "",
    @Document.StringProperty
    val displayName: String = "",
    @Document.StringProperty
    val addressType: String = "",
    @Document.StringProperty
    val polygonCoordinates: String = "",
    @Document.Score
    val score: Int = 0,
)