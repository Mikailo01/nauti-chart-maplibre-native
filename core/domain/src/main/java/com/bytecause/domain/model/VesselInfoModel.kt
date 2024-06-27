package com.bytecause.domain.model

/**
 * Contains detailed informations about vessel
 * */
data class VesselInfoModel(
    val id: Int = 0,
    val latitude: String = "",
    val longitude: String = "",
    val name: String = "",
    val type: String = "",
    val heading: String = "",
    val speed: String = "",
    val flag: String = "",
    val mmsi: String = "",
    val length: String = "",
    val eta: String = "",
    val timeStamp: Long = 0L
)
