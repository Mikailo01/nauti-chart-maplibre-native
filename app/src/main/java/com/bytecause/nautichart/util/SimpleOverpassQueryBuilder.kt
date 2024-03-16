package com.bytecause.nautichart.util

import android.os.Parcel
import android.os.Parcelable
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint


// search types to distinguish what type to search in query
sealed class SearchTypes : Parcelable {

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        when (this) {
            is Amenity -> {
                parcel.writeStringArray(value)
            }

            is UnionSet -> {
                parcel.writeStringArray(value)
            }
        }
    }

    // e.x.: [amenity=bar], [amenity=restaurant], ...
    data class Amenity(val value: Array<String>) : SearchTypes() {

        constructor(parcel: Parcel) : this(parcel.createStringArray() ?: emptyArray())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Amenity

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeStringArray(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Amenity> {
            override fun createFromParcel(parcel: Parcel): Amenity {
                return Amenity(parcel)
            }

            override fun newArray(size: Int): Array<Amenity?> {
                return arrayOfNulls(size)
            }
        }
    }

    // selects all elements that have a tag with a certain key and an arbitrary value.
    // e.x.: [seamark:type], [public_transport], ...
    data class UnionSet(val value: Array<String>) : SearchTypes() {

        constructor(parcel: Parcel) : this(parcel.createStringArray() ?: emptyArray())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnionSet

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeStringArray(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UnionSet> {
            override fun createFromParcel(parcel: Parcel): UnionSet {
                return UnionSet(parcel)
            }

            override fun newArray(size: Int): Array<UnionSet?> {
                return arrayOfNulls(size)
            }
        }
    }
}


/** Simple query builder for Overpass API interpreter. **/
class SimpleOverpassQueryBuilder {

    // supported output formats
    enum class FormatTypes {
        JSON, XML
    }

    private val queryBuilder = StringBuilder()

    // constructor for radius search
    constructor(
        format: FormatTypes,
        timeoutInSeconds: Int,
        type: String,
        search: SearchTypes,
        radiusInMeters: Int,
        geoPoint: GeoPoint
    ) {
        format(format)
        timeout(timeoutInSeconds)
        queryBuilder.append("(")
        when (search) {
            is SearchTypes.Amenity -> {
                type(type)
                amenity(amenity = search)
                radius(radiusInMeters, geoPoint)
            }

            is SearchTypes.UnionSet -> {
                for (value in search.value) {
                    type(type)
                    unionSet(value)
                    radius(radiusInMeters, geoPoint)
                }
            }
        }
        queryBuilder.append(");")
        if (type == "relation" || type == "way") queryBuilder.append("(._;>;);")
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
        type: String,
        search: SearchTypes
    ) {
        format(format)
        timeout(timeoutInSeconds)
        region(regionNameList)
        queryBuilder.append("(")
        when (search) {
            is SearchTypes.Amenity -> {
                type(type)
                amenity(search)
            }

            is SearchTypes.UnionSet -> {
                search.value.forEachIndexed { index, s ->
                    type(type)
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
        type: String,
        adminLevel: Int
    ) {
        format(format)
        timeout(timeoutInSeconds)
        geocodeAreaISO(geocodeAreaISO)
        type(type)
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
    private fun radius(radius: Int, geoPoint: GeoPoint?) {
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
