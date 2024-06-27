package com.bytecause.pois.data.repository.abstractions

interface CountryDataExtractSizeRepository {
    suspend fun fetchSize(continentName: String, country: String? = null): Map<String, String>
}