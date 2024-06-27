package com.bytecause.domain.util

import com.bytecause.domain.model.LatLngModel

class OverpassQueryBuilderDsl {
    enum class FormatTypes {
        JSON, XML
    }

    companion object {
        fun format(format: FormatTypes) = FormatStep(format)
    }

    class FormatStep(private val format: FormatTypes) {
        fun timeout(timeoutInSeconds: Int) = TimeoutStep(format, timeoutInSeconds)
    }

    class TimeoutStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int
    ) {
        fun region(regionNameList: List<String>) = RegionStep(format, timeoutInSeconds, regionNameList)
        fun geocodeAreaISO(isoCode: String) = GeocodeAreaStep(format, timeoutInSeconds, isoCode)
        fun radiusSearch(
            type: String,
            search: SearchTypes,
            radiusInMeters: Int,
            geoPoint: LatLngModel
        ) = OverpassQueryBuilderDsl().RadiusSearchStep(format, timeoutInSeconds, type, search, radiusInMeters, geoPoint)
    }

    class RegionStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>
    ) {
        fun type(type: String) = TypeStep(format, timeoutInSeconds, regionNameList, type)
    }

    class GeocodeAreaStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String
    ) {
        fun type(type: String) = AdminLevelStep(format, timeoutInSeconds, isoCode, type)
    }

    inner class RadiusSearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val type: String,
        private val search: SearchTypes,
        private val radiusInMeters: Int,
        private val geoPoint: LatLngModel
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            queryBuilder.append("(")
            when (search) {
                is SearchTypes.Amenity -> {
                    appendType(queryBuilder, type)
                    appendAmenity(queryBuilder, search)
                    appendRadius(queryBuilder, radiusInMeters, geoPoint)
                }
                is SearchTypes.UnionSet -> {
                    for (value in search.value) {
                        appendType(queryBuilder, type)
                        appendUnionSet(queryBuilder, value)
                        appendRadius(queryBuilder, radiusInMeters, geoPoint)
                    }
                }
            }
            queryBuilder.append(");")
            if (type == "relation" || type == "way") queryBuilder.append("(._;>;);")
            appendOut(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    class TypeStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>,
        private val type: String
    ) {
        fun search(search: SearchTypes) = OverpassQueryBuilderDsl().SearchStep(format, timeoutInSeconds, regionNameList, type, search)
    }

    class AdminLevelStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String,
        private val type: String
    ) {
        fun adminLevel(adminLevel: Int) = OverpassQueryBuilderDsl().AdminLevelSearchStep(format, timeoutInSeconds, isoCode, type, adminLevel)
    }

    inner class AdminLevelSearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String,
        private val type: String,
        private val adminLevel: Int
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            appendGeocodeAreaISO(queryBuilder, isoCode)
            appendType(queryBuilder, type)
            appendAdminLevel(queryBuilder, adminLevel)
            appendSearchArea(queryBuilder, true)
            queryBuilder.append(");")
            appendOutTags(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    inner class SearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>,
        private val type: String,
        private val search: SearchTypes
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            appendRegion(queryBuilder, regionNameList)
            queryBuilder.append("(")
            when (search) {
                is SearchTypes.Amenity -> {
                    appendType(queryBuilder, type)
                    appendAmenity(queryBuilder, search)
                }
                is SearchTypes.UnionSet -> {
                    search.value.forEachIndexed { index, s ->
                        appendType(queryBuilder, type)
                        appendSearchArea(queryBuilder, false)
                        appendUnionSet(queryBuilder, s)
                        if (index == search.value.size - 1) queryBuilder.append(";")
                    }
                }
            }
            queryBuilder.append(");")
            appendOut(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    private fun appendFormatAndTimeout(builder: StringBuilder, format: FormatTypes, timeout: Int) {
        when (format) {
            FormatTypes.JSON -> builder.append("[out:json];")
            FormatTypes.XML -> builder.append("[out:xml];")
        }
        builder.append("[timeout:$timeout];")
    }

    private fun appendGeocodeAreaISO(builder: StringBuilder, isoCode: String) {
        builder.append("(area[\"ISO3166-1\"=\"${isoCode}\"]->.searchArea;")
    }

    private fun appendAdminLevel(builder: StringBuilder, level: Int) {
        builder.append("[\"admin_level\"=\"$level\"]")
    }

    private fun appendSearchArea(builder: StringBuilder, lineEnd: Boolean) {
        builder.append("(area.searchArea)")
        if (lineEnd) builder.append(";")
    }

    private fun appendRegion(builder: StringBuilder, regionNameList: List<String>) {
        if (regionNameList.size == 1) {
            builder.append("area[\"name\"=\"${regionNameList.first()}\"]->.searchArea;")
        } else {
            builder.append("area[\"name\"~\"")
            regionNameList.joinTo(builder, separator = "|")
            builder.append("\"]->.searchArea;")
        }
    }

    private fun appendType(builder: StringBuilder, type: String) {
        builder.append(type)
    }

    private fun appendRadius(builder: StringBuilder, radius: Int, geoPoint: LatLngModel?) {
        geoPoint ?: return
        builder.append("(around:$radius,${geoPoint.latitude},${geoPoint.longitude});")
    }

    private fun appendOut(builder: StringBuilder) {
        builder.append("out;")
    }

    private fun appendOutTags(builder: StringBuilder) {
        builder.append("out tags;")
    }

    private fun appendAmenity(builder: StringBuilder, amenity: SearchTypes.Amenity) {
        if (amenity.value.size == 1) {
            builder.append("[amenity=${amenity.value.first()}]")
            return
        }
        builder.append("[amenity~\"${amenity.value.joinToString("|")}\"]")
    }

    private fun appendUnionSet(builder: StringBuilder, s: String) {
        builder.append("[\"$s\"](if: count_tags() > 0)")
    }
}

sealed class SearchTypes {
    class Amenity(val value: List<String>) : SearchTypes()
    class UnionSet(val value: List<String>) : SearchTypes()
}

fun main() {
    val query1 = OverpassQueryBuilderDsl
        .format(OverpassQueryBuilderDsl.FormatTypes.JSON)
        .timeout(60)
        .region(listOf("Severovýchod"))
        .type("node")
        .search(SearchTypes.Amenity(listOf("shop", "cafe")))
        .build()

    println(query1)

    val query2 = OverpassQueryBuilderDsl
        .format(OverpassQueryBuilderDsl.FormatTypes.XML)
        .timeout(120)
        .geocodeAreaISO("DE")
        .type("relation")
        .adminLevel(4)
        .build()

    println(query2)

    val query3 = OverpassQueryBuilderDsl
        .format(OverpassQueryBuilderDsl.FormatTypes.JSON)
        .timeout(60)
        .radiusSearch("node", SearchTypes.Amenity(listOf("shop")), 1000, LatLngModel(52.5200, 13.4050))
        .build()

    println(query3)
}
