package com.bytecause.nautichart.overpassquerybuilder

import android.util.Log
import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.model.SearchTypes
import com.bytecause.domain.util.SimpleOverpassQueryBuilder
import com.bytecause.util.string.StringUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class QueryBuilderTest {
    @Test
    fun radiusSearchQuery() {
        val geoPoint = LatLngModel(46.45523, 14.65742)

        val singleAmenityNodeQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.Amenity(arrayOf("restaurant")),
                radiusInMeters = 30000,
                geoPoint = geoPoint,
            ).getQuery()

        val multipleAmenityNodeQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.Amenity(
                    arrayOf(
                        "restaurant",
                        "pub",
                        "bar"
                    )
                ),
                radiusInMeters = 30000,
                geoPoint = geoPoint,
            ).getQuery()

        val singleUnionNodeQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.UnionSet(arrayOf("tourism")),
                radiusInMeters = 30000,
                geoPoint = geoPoint,
            ).getQuery()

        val multipleUnionNodeQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.UnionSet(
                    arrayOf(
                        "tourism",
                        "activity",
                        "public_transport"
                    )
                ),
                radiusInMeters = 30000,
                geoPoint = geoPoint,
            ).getQuery()

        val multipleUnionRelationQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.UnionSet(
                    arrayOf(
                        "tourism",
                        "activity",
                        "public_transport"
                    )
                ),
                radiusInMeters = 30000,
                geoPoint = geoPoint,
            ).getQuery()

        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "node[amenity=restaurant](around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                ");out;",
            singleAmenityNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "node[amenity~\"restaurant|pub|bar\"](around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                ");" +
                "out;",
            multipleAmenityNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "node[\"tourism\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                ");" +
                "out;",
            singleUnionNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "node[\"tourism\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                "node[\"activity\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                "node[\"public_transport\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                ");" +
                "out;",
            multipleUnionNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "relation[\"tourism\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                "relation[\"activity\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                "relation[\"public_transport\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                ");" +
                "(._;>;);" +
                "out;",
            multipleUnionRelationQuery,
        )
    }

    @Test
    fun searchByRegionQuery() {
        val searchByRegionQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                regionNameList = listOf("Jihomoravský kraj"),
                type = SimpleOverpassQueryBuilder.Type.Node,
                search = SearchTypes.UnionSet(StringUtil.searchTypesStringList.toTypedArray()),
            ).getQuery()

        Log.d(com.bytecause.util.extensions.TAG(this), searchByRegionQuery)

        assertEquals(
            "[out:json][timeout:120];" +
                "area[\"name\"=\"Jihomoravský kraj\"]->.searchArea;" +
                "(" +
                "node(area.searchArea)[\"shop\"](if: count_tags() > 0);" +
                "node(area.searchArea)[\"amenity\"](if: count_tags() > 0);" +
                "node(area.searchArea)[\"leisure\"](if: count_tags() > 0);" +
                "node(area.searchArea)[\"tourism\"](if: count_tags() > 0);" +
                "node(area.searchArea)[\"seamark:type\"](if: count_tags() > 0);" +
                "node(area.searchArea)[\"public_transport\"](if: count_tags() > 0);" +
                ");" +
                "out;",
            searchByRegionQuery,
        )
    }

    @Test
    fun searchRegionsQuery() {
        val searchRegionsQuery =
            SimpleOverpassQueryBuilder(
                format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                timeoutInSeconds = 120,
                geocodeAreaISO = "CZ",
                type = SimpleOverpassQueryBuilder.Type.Node,
                adminLevel = 6,
            ).getQuery()

        assertEquals(
            "[out:json][timeout:120];" +
                "(" +
                "area[\"ISO3166-1\"=\"CZ\"]->.searchArea;" +
                "node[\"admin_level\"=\"6\"](area.searchArea);" +
                ");" +
                "out tags;",
            searchRegionsQuery,
        )
    }
}
