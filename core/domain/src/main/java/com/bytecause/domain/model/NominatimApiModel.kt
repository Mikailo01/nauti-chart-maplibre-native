package com.bytecause.domain.model

import com.google.gson.annotations.SerializedName

@Suppress("SpellCheckingInspection")
data class NominatimApiModel(
    @SerializedName("place_id") val placeId: Long = 0L,
    val licence: String = "",
    @SerializedName("osm_type") val osmType: String = "",
    @SerializedName("osm_id") val osmId: Long = 0L,
    val lat: Double,
    val lon: Double,
    val category: String = "",
    val type: String = "",
    @SerializedName("place_rank") val placeRank: Int = 0,
    val importance: Double = 0.0,
    @SerializedName("addresstype") val addressType: String = "",
    val name: String = "",
    @SerializedName("display_name") val displayName: String = "",
    @SerializedName("boundingbox") val boundingBox: List<String> = listOf(),
    @SerializedName("geokml") val polygonCoordinates: String = ""
)