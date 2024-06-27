package com.bytecause.domain.util

import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.model.SearchTypes


/** Simple query builder for Overpass API interpreter. **/
class SimpleOverpassQueryBuilder {

    // supported output formats
    enum class FormatTypes {
        JSON, XML
    }

    enum class Type {
        Node,
        Relation,
        Way
    }

    private val queryBuilder = StringBuilder()

    // constructor for radius search
    constructor(
        format: FormatTypes,
        timeoutInSeconds: Int,
        type: Type,
        search: SearchTypes,
        radiusInMeters: Int,
        geoPoint: LatLngModel
    ) {
        val typeName = type.name.lowercase()

        format(format)
        timeout(timeoutInSeconds)
        queryBuilder.append("(")
        when (search) {
            is SearchTypes.Amenity -> {
                type(typeName)
                amenity(amenity = search)
                radius(radiusInMeters, geoPoint)
            }

            is SearchTypes.UnionSet -> {
                for (value in search.value) {
                    type(typeName)
                    unionSet(value)
                    radius(radiusInMeters, geoPoint)
                }
            }
        }
        queryBuilder.append(");")
        if (typeName == "relation" || typeName == "way") queryBuilder.append("(._;>;);")
        out()
    }

    /**
     * e.x.:
     * [out:json];
     * area["name"="Severovýchod"]->.searchArea;
     * node(area.searchArea)["amenity"];
     * out center;
     **/
    constructor(
        format: FormatTypes,
        timeoutInSeconds: Int,
        regionNameList: List<String>,
        type: Type,
        search: SearchTypes
    ) {
        val typeName = type.name.lowercase()

        format(format)
        timeout(timeoutInSeconds)
        region(regionNameList)
        queryBuilder.append("(")
        when (search) {
            is SearchTypes.Amenity -> {
                type(typeName)
                amenity(search)
            }

            is SearchTypes.UnionSet -> {
                search.value.forEachIndexed { index, s ->
                    type(typeName)
                    searchArea(false)
                    unionSet(s)
                    if (index == search.value.size - 1) queryBuilder.append(";")
                }
            }
        }
        queryBuilder.append(");")
        out()
    }

    constructor(
        format: FormatTypes,
        timeoutInSeconds: Int,
        geocodeAreaISO: String,
        type: Type,
        adminLevel: Int
    ) {
        val typeName = type.name.lowercase()

        format(format)
        timeout(timeoutInSeconds)
        geocodeAreaISO(geocodeAreaISO)
        type(typeName)
        adminLevel(adminLevel)
        searchArea(true)
        queryBuilder.append(");")
        outTags()
    }

    // Output format.
    private fun format(format: FormatTypes) {
        when (format) {
            FormatTypes.JSON -> queryBuilder.append("[out:json]")
            FormatTypes.XML -> queryBuilder.append("[out:xml]")
        }
    }

    private fun unionSet(s: String) {
            queryBuilder.append("[\"$s\"]")
            filterTagsOnly()
    }

    private fun geocodeAreaISO(isoCode: String) {
        queryBuilder.append("(area[\"ISO3166-1\"=\"${isoCode}\"]->.searchArea;")
    }

    private fun adminLevel(level: Int) {
        queryBuilder.append("[\"admin_level\"=\"$level\"]")
    }

    private fun filterTagsOnly() {
        queryBuilder.append("(if: count_tags() > 0)")
    }

    // e.x.: [!"amenity"]
    private fun filterOutTagKey(tagKey: String) {
        queryBuilder.append("[!\"$tagKey\"]")
    }

    private fun region(regionNameList: List<String>) {
        if (regionNameList.size == 1) queryBuilder.append("area[\"name\"=\"${regionNameList.first()}\"]->.searchArea;")
        else {
            queryBuilder.append("area[\"name\"~\"")
            for (index in regionNameList.indices) {
                queryBuilder.apply {
                    append(regionNameList[index])
                    if (index != regionNameList.size - 1) append("|")
                    else append("\"]->.searchArea;")
                }
            }
        }
    }

    private fun searchArea(lineEnd: Boolean) {
        queryBuilder.append("(area.searchArea)")
        if (lineEnd) queryBuilder.append(";")
    }

    // Node, way, relation.
    private fun type(type: String) {
        if (queryBuilder.last() == '(' || queryBuilder.last() == ';') {
            queryBuilder.append(type)
        } else queryBuilder.append(";$type")
    }

    // e.x.: around:30000 ...
    private fun radius(radius: Int, geoPoint: LatLngModel?) {
        geoPoint ?: return
        queryBuilder.append("(around:$radius,${geoPoint.latitude},${geoPoint.longitude});")
    }

    private fun out() {
        queryBuilder.append("out;")
    }

    private fun outTags() {
        queryBuilder.append("out tags;")
    }

    private fun timeout(timeout: Int) {
        queryBuilder.append("[timeout:$timeout];")
    }

    // search for amenity ([amenity=shop], [amenity~"pub|restaurant|shop..."])
    private fun amenity(amenity: SearchTypes.Amenity) {
        if (amenity.value.size == 1) {
            queryBuilder.append("[amenity=${amenity.value.first()}]")
            return
        }
        queryBuilder.append("[amenity~\"${amenity.value.joinToString("|")}\"]")
    }

    // getter method for finalized query string
    fun getQuery(): String = queryBuilder.toString().trim()
}
