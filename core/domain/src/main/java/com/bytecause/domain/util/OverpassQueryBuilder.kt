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
        fun region(regionName: String) =
            RegionStep(format, timeoutInSeconds, regionName)

        fun geocodeAreaISO(isoCode: String) = GeocodeAreaStep(format, timeoutInSeconds, isoCode)
        fun radius() = RadiusStep(format, timeoutInSeconds)
        fun wholeWorld() = WholeWorldStep(format, timeoutInSeconds)
    }

    class RegionStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionName: String
    ) {
        fun type(type: Type) = TypeStep(format, timeoutInSeconds, regionName, type)
    }

    class TypeStep(
        private val format: FormatTypes,
        private val timeoutInSeconds: Int,
        private val regionName: String,
        private val type: Type
    ) {
        fun search(search: SearchTypes) = OverpassQueryBuilder().SearchStep(
            format,
            timeoutInSeconds,
            regionName,
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
        private val regionName: String,
        private val type: Type,
        private val search: SearchTypes
    ) {
        fun build(): String {
            val queryBuilder = StringBuilder()
            appendFormatAndTimeout(queryBuilder, format, timeoutInSeconds)
            appendRegion(queryBuilder, regionName)
            queryBuilder.append("(")
            when (search) {
                is SearchTypes.Amenity -> {
                    appendType(queryBuilder, type.lowerCaseName)
                    appendAmenity(queryBuilder, search)
                }

                is SearchTypes.UnionSet -> {
                    search.list.forEachIndexed { index, s ->
                        appendType(queryBuilder, type.lowerCaseName)
                        appendSearchArea(queryBuilder, false)

                        val key = search.excludeFilters.keys.toList().takeIf { it.isNotEmpty() }
                            ?.get(index)
                        appendUnionSetWithFilter(
                            queryBuilder,
                            s,
                            if (key == null) null
                            else {
                                search.excludeFilters.takeIf { !it[key].isNullOrEmpty() }
                                    ?.run { Pair(key, this[key]!!) }
                            }
                        )

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
                    search.list.forEachIndexed { index, s ->
                        appendType(queryBuilder, type.lowerCaseName)

                        val key = search.excludeFilters.keys.toList().takeIf { it.isNotEmpty() }
                            ?.get(index)
                        appendUnionSetWithFilter(
                            queryBuilder,
                            s,
                            if (key == null) null
                            else {
                                search.excludeFilters.takeIf { !it[key].isNullOrEmpty() }
                                    ?.run { Pair(key, this[key]!!) }
                            }
                        )

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
                    search.list.forEachIndexed { index, s ->
                        appendType(queryBuilder, type.lowerCaseName)

                        val key = search.excludeFilters.keys.toList().takeIf { it.isNotEmpty() }
                            ?.get(index)
                        appendUnionSetWithFilter(
                            queryBuilder,
                            s,
                            if (key == null) null
                            else {
                                search.excludeFilters.takeIf { !it[key].isNullOrEmpty() }
                                    ?.run { Pair(key, this[key]!!) }
                            }
                        )

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

    private fun appendRegion(builder: StringBuilder, regionName: String) {
        builder.append("area[\"name\"=\"${regionName}\"]->.searchArea;")
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

    private fun appendUnionSetWithFilter(
        builder: StringBuilder,
        s: String,
        filters: Pair<String, List<String>>? = null
    ) {
        if (filters == null) {
            builder.append("[$s](if: count_tags() > 0)")
        } else {
            builder.append("[$s](if: count_tags() > 0")

            for (filter in filters.second) {
                builder.append(" && t[${filters.first}] != $filter")
            }
            builder.append(")")
        }
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

        var excludeFilters: Map<String, List<String>> = emptyMap()
            private set

        /**
         * Filter the search objects based on the provided filters (values).
         *
         * This function takes a variable number of `List<String>` and combines those values with the
         * existing values in the `keys` list to create a new list of formatted search parameters.
         * If filter list contains single value, then each formatted search parameter
         * is a string in the format: `"<key>"~"<value>"`. If filter list contains multiple values, then
         * each formatted search parameter is a string in format: `"<key>"~"<value1>|<value2>| ... |valueN"`.
         *
         * @param filters A variable number of filter lists. Each list with filters corresponding to an
         * index of the key in UnionSet.
         * @return The current instance of [UnionSet] with the updated list of formatted search parameters.
         *
         * Example usage:
         * ```
         * val query: String = OverpassQueryBuilder
         *                     .format(
         *                     OverpassQueryBuilder.FormatTypes.JSON
         *                     )
         *                     .timeout(60)
         *                     .wholeWorld()
         *                     .search(
         *                         OverpassQueryBuilder.Type.Node,
         *                         SearchTypes.UnionSet(
         *                             listOf(
         *                                 "leisure",
         *                                 "tourism"
         *                             )
         *                         ).filter(
         *                          emptyList(), // if we don't want to apply any filters to this object, we must pass emptyList() or IllegalArgumentException will be thrown
         *                          listOf("hotel")
         *                         )
         *                     )
         *                     .build()
         * ```
         *
         * Notice that the order of `List<String>` filters must correspond to the order of key list passed into UnionSet object.
         */
        fun filter(vararg filters: List<String>): UnionSet {
            when {
                filters.size > keys.size -> {
                    throw IllegalArgumentException(
                        "Number of filter lists must match the number of keys."
                    )
                }

                filters.size < keys.size -> {
                    throw IllegalArgumentException(
                        "Number of filter lists must match the number of keys. Pass empty list if you don't want to apply filters to it."
                    )
                }

                else -> {
                    list = keys.zip(filters) { key, filterList ->
                        if (filterList.isEmpty()) {
                            "\"$key\""
                        } else {
                            val filterExpression = filterList.joinToString(separator = "|") { it }
                            "\"$key\"~\"$filterExpression\""
                        }
                    }
                    return this
                }
            }
        }

        /**
         * Filter out the search objects based on the provided filters (values).
         *
         * Example usage:
         *```
         * val query: String = OverpassQueryBuilder
         *                     .format(
         *                     OverpassQueryBuilder.FormatTypes.JSON
         *                     )
         *                     .timeout(60)
         *                     .wholeWorld()
         *                     .search(
         *                         OverpassQueryBuilder.Type.Node,
         *                         SearchTypes.UnionSet(
         *                             listOf(
         *                                 "tourism",
         *                                 "seamark:type"
         *                             )
         *                         ).filterNot(
         *                          emptyList(), // if we don't want to apply any filters to this object, we must pass emptyList() or IllegalArgumentException will be thrown
         *                          listOf("harbour")
         *                         )
         *                     )
         *                     .build()
         * ```
         *
         * Notice that the order of `List<String>` filters must correspond to the order of key list passed into UnionSet object.
         */
        fun filterNot(vararg filters: List<String>): UnionSet {
            when {
                filters.size > keys.size -> {
                    throw IllegalArgumentException(
                        "Number of filter lists must match the number of keys."
                    )
                }

                filters.size < keys.size -> {
                    throw IllegalArgumentException(
                        "Number of filter lists must match the number of keys. Pass empty list if you don't want to apply filters to it."
                    )
                }

                else -> {
                    filters.forEachIndexed { index, filterList ->
                        excludeFilters += mapOf("\"${keys[index]}\"" to filterList.map { "\"${it}\"" })
                    }
                    return this
                }
            }
        }
    }
}