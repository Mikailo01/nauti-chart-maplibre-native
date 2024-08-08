package com.bytecause.domain.util

import com.bytecause.domain.model.LatLngModel

class OverpassQueryBuilder {
    enum class FormatTypes {
        JSON, XML
    }

    enum class Type {
        Node,
        Relation,
        Way;

        val lowerCaseName = this.name.lowercase()
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
        fun region(regionNameList: List<String>) =
            RegionStep(format, timeoutInSeconds, regionNameList)

        fun geocodeAreaISO(isoCode: String) = GeocodeAreaStep(format, timeoutInSeconds, isoCode)
        fun radius() = RadiusStep(format, timeoutInSeconds)
        fun wholeWorld() = WholeWorldStep(format, timeoutInSeconds)
    }

    class RegionStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>
    ) {
        fun type(type: Type) = TypeStep(format, timeoutInSeconds, regionNameList, type)
    }

    class TypeStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>,
        private val type: Type
    ) {
        fun search(search: SearchTypes) = OverpassQueryBuilder().SearchStep(
            format,
            timeoutInSeconds,
            regionNameList,
            type,
            search
        )
    }

    class GeocodeAreaStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String
    ) {
        fun type(type: Type) = AdminLevelStep(format, timeoutInSeconds, isoCode, type)
    }

    class AdminLevelStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String,
        private val type: Type
    ) {
        fun adminLevel(adminLevel: Int) = OverpassQueryBuilder().AdminLevelSearchStep(
            format,
            timeoutInSeconds,
            isoCode,
            type,
            adminLevel
        )
    }

    class RadiusStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int
    ) {
        fun search(type: Type, search: SearchTypes) =
            AreaStep(format, timeoutInSeconds, type, search)
    }

    class AreaStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val type: Type,
        private val search: SearchTypes
    ) {
        fun area(radiusInMeters: Int, center: LatLngModel) =
            OverpassQueryBuilder().RadiusSearchStep(
                format,
                timeoutInSeconds,
                type,
                search,
                radiusInMeters,
                center
            )
    }

    class WholeWorldStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int
    ) {
        fun search(type: Type, search: SearchTypes) =
            OverpassQueryBuilder().WholeWorldSearchStep(format, timeoutInSeconds, type, search)
    }

    inner class SearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionNameList: List<String>,
        private val type: Type,
        private val search: SearchTypes
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            appendRegion(queryBuilder, regionNameList)
            queryBuilder.append("(")
            when (search) {
                is SearchTypes.Amenity -> {
                    appendType(queryBuilder, type.lowerCaseName)
                    appendAmenity(queryBuilder, search)
                }

                is SearchTypes.UnionSet -> {
                    search.list.forEach { s ->
                        appendType(queryBuilder, type.lowerCaseName)
                        appendSearchArea(queryBuilder, false)
                        appendUnionSet(queryBuilder, s)
                        queryBuilder.append(";")
                    }
                }
            }
            queryBuilder.append(");")
            appendOut(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    inner class AdminLevelSearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val isoCode: String,
        private val type: Type,
        private val adminLevel: Int
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            appendGeocodeAreaISO(queryBuilder, isoCode)
            appendType(queryBuilder, type.lowerCaseName)
            appendAdminLevel(queryBuilder, adminLevel)
            appendSearchArea(queryBuilder, true)
            queryBuilder.append(");")
            appendOutTags(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    inner class RadiusSearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val type: Type,
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
                    appendType(queryBuilder, type.lowerCaseName)
                    appendAmenity(queryBuilder, search)
                    appendRadius(queryBuilder, radiusInMeters, geoPoint)
                }

                is SearchTypes.UnionSet -> {
                    for (value in search.list) {
                        appendType(queryBuilder, type.lowerCaseName)
                        appendUnionSet(queryBuilder, value)
                        appendRadius(queryBuilder, radiusInMeters, geoPoint)
                    }
                }
            }
            queryBuilder.append(");")
            if (type == Type.Relation || type == Type.Way) queryBuilder.append("(._;>;);")
            appendOut(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    inner class WholeWorldSearchStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val type: Type,
        private val search: SearchTypes
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            queryBuilder.append("(")
            when (search) {
                is SearchTypes.Amenity -> {
                    appendType(queryBuilder, type.lowerCaseName)
                    appendAmenity(queryBuilder, search)
                }

                is SearchTypes.UnionSet -> {
                    for (value in search.list) {
                        appendType(queryBuilder, type.lowerCaseName)
                        appendUnionSet(queryBuilder, value)
                        queryBuilder.append(";")
                    }
                }
            }
            queryBuilder.append(");")
            if (type == Type.Relation || type == Type.Way) queryBuilder.append("(._;>;);")
            appendOut(queryBuilder)
            return queryBuilder.toString().trim()
        }
    }

    private fun appendFormatAndTimeout(builder: StringBuilder, format: FormatTypes, timeout: Int) {
        when (format) {
            FormatTypes.JSON -> builder.append("[out:json]")
            FormatTypes.XML -> builder.append("[out:xml]")
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
        if (amenity.values.size == 1) {
            builder.append("[amenity=${amenity.values.first()}]")
            return
        }
        builder.append("[amenity~\"${amenity.values.joinToString("|")}\"]")
    }

    private fun appendUnionSet(builder: StringBuilder, s: String) {
        builder.append("[$s](if: count_tags() > 0)")
    }
}

/**
 * A sealed class representing different types of search parameters.
 */
sealed class SearchTypes {

    /**
     * A class representing a search type for amenities.
     * Use this if you want to search specific values under amenity key
     * e.g.: ```["amenity" = "shop"], ["amenity" = "restaurant"]```
     *
     * Takes amenity key values as parameter.
     */
    class Amenity(val values: List<String>) : SearchTypes()

    /**
     * A class representing a union set for searching specific keys in the object.
     * Use this if you want to search objects by keys e.g.:```["tourism"], ["amenity"], ["public_transport"]```
     *
     * Takes key list as parameter.
     */
    class UnionSet(private val keys: List<String>) : SearchTypes() {
        /**
         * A list of formatted search parameter strings.
         * This list is initialized by mapping each value in the input list
         * to a quoted string.
         */
        var list: List<String> = keys.map { "\"$it\"" }
            private set

        /**
         * Filters the search objects based on the provided filters (values).
         *
         * This function takes a variable number of filter strings and combines them
         * with the existing values in the `value` list to create a new list of formatted
         * search parameters. Each formatted search parameter is a string in the format
         * `"<key>"="<value>"`.
         *
         * @param filters A variable number of filter strings.
         * @return The current instance of [UnionSet] with the updated list of formatted search parameters.
         * Example usage:
         * ```
         * val query: String = OverpassQueryBuilder
         *                     .format(OverpassQueryBuilder.FormatTypes.JSON)
         *                     .timeout(60)
         *                     .wholeWorld()
         *                     .search(
         *                         OverpassQueryBuilder.Type.Node,
         *                         SearchTypes.UnionSet(
         *                             listOf(
         *                                 "seamark:type",
         *                                 "leisure"
         *                             )
         *                         ).filter("harbour", "marina")
         *                     )
         *                     .build()
         * ```
         *
         * Notice that the order of filters must correspond to the order of key list passed into UnionSet object.
         */
        fun filter(vararg filters: String): UnionSet {
            list = keys.zip(filters) { v, f -> "\"$v\"=\"$f\"" }
            return this
        }
    }
}