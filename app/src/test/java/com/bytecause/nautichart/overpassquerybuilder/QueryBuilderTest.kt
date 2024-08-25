package com.bytecause.nautichart.overpassquerybuilder

import com.bytecause.domain.model.LatLngModel
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.domain.util.SearchTypes
import com.bytecause.util.poi.PoiUtil.searchTypesStringList
import com.bytecause.util.string.StringUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class QueryBuilderTest {
    @Test
    fun `queries for searching pois in given radius`() {
        val geoPoint = LatLngModel(46.45523, 14.65742)

        val singleAmenityNodeQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .radius()
            .search(
                OverpassQueryBuilder.Type.Node,
                SearchTypes.Amenity(
                    listOf("restaurant")
                )
            )
            .area(30000, geoPoint)
            .build()

        val multipleAmenityNodeQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .radius()
            .search(
                OverpassQueryBuilder.Type.Node,
                SearchTypes.Amenity(
                    listOf(
                        "restaurant",
                        "pub",
                        "bar"
                    )
                )
            )
            .area(30000, geoPoint)
            .build()

        val singleUnionNodeQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .radius()
            .search(
                OverpassQueryBuilder.Type.Node, SearchTypes.UnionSet(listOf("tourism"))
            )
            .area(30000, geoPoint)
            .build()

        val multipleUnionNodeQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .radius()
            .search(
                OverpassQueryBuilder.Type.Node,
                SearchTypes.UnionSet(
                    listOf(
                        "tourism",
                        "activity",
                        "public_transport"
                    )
                )
            )
            .area(30000, geoPoint)
            .build()

        val multipleUnionRelationQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .radius()
            .search(
                OverpassQueryBuilder.Type.Relation,
                SearchTypes.UnionSet(
                    listOf(
                        "tourism",
                        "activity",
                        "public_transport"
                    )
                )
            )
            .area(30000, geoPoint)
            .build()

        assertEquals(
            "[out:json][timeout:60];" +
                    "(" +
                    "node[amenity=restaurant](around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    ");out;",
            singleAmenityNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:60];" +
                    "(" +
                    "node[amenity~\"restaurant|pub|bar\"](around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    ");" +
                    "out;",
            multipleAmenityNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:60];" +
                    "(" +
                    "node[\"tourism\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    ");" +
                    "out;",
            singleUnionNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:60];" +
                    "(" +
                    "node[\"tourism\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    "node[\"activity\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    "node[\"public_transport\"](if: count_tags() > 0)(around:30000,${geoPoint.latitude},${geoPoint.longitude});" +
                    ");" +
                    "out;",
            multipleUnionNodeQuery,
        )
        assertEquals(
            "[out:json][timeout:60];" +
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
    fun `query for searching pois in given region`() {
        val searchByRegionQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .region(listOf("Jihomoravský kraj"))
            .type(OverpassQueryBuilder.Type.Node)
            .search(SearchTypes.UnionSet(searchTypesStringList))
            .build()

        assertEquals(
            "[out:json][timeout:60];" +
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
    fun `query for searching country's regions`() {
        val searchRegionsQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(60)
            .geocodeAreaISO("CZ")
            .type(OverpassQueryBuilder.Type.Node)
            .adminLevel(6)
            .build()

        assertEquals(
            "[out:json][timeout:60];" +
                    "(" +
                    "area[\"ISO3166-1\"=\"CZ\"]->.searchArea;" +
                    "node[\"admin_level\"=\"6\"](area.searchArea);" +
                    ");" +
                    "out tags;",
            searchRegionsQuery,
        )
    }

    @Test
    fun `query for searching pois in given region with applied filters`() {
        val filterNotQuery = OverpassQueryBuilder
            .format(OverpassQueryBuilder.FormatTypes.JSON)
            .timeout(120)
            .region(listOf("Jihovýchod"))
            .type(OverpassQueryBuilder.Type.Node)
            .search(
                SearchTypes.UnionSet(searchTypesStringList)
                    .filterNot(
                        emptyList(),
                        listOf(
                            "clock",
                            "fixme",
                            "public_building",
                            "smoking_area",
                            "internet_kiosk"
                        ),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList()
                    )

            )
            .build()

        val filterQuery: String = OverpassQueryBuilder
            .format(
                OverpassQueryBuilder.FormatTypes.JSON
            )
            .timeout(120)
            .region(listOf("Jihovýchod"))
            .type(OverpassQueryBuilder.Type.Node)
            .search(
                SearchTypes.UnionSet(
                    listOf(
                        "leisure",
                        "tourism"
                    )
                ).filter(
                    emptyList(),
                    listOf("hotel")
                )
            )
            .build()

        assertEquals(
            "[out:json][timeout:120];" +
                    "area[\"name\"=\"Jihovýchod\"]->.searchArea;" +
                    "(" +
                    "node(area.searchArea)[\"shop\"](if: count_tags() > 0);" +
                    "node(area.searchArea)[\"amenity\"](if: count_tags() > 0 && t[\"amenity\"] != \"clock\" " +
                    "&& t[\"amenity\"] != \"fixme\" && t[\"amenity\"] != \"public_building\" " +
                    "&& t[\"amenity\"] != \"smoking_area\" && t[\"amenity\"] != \"internet_kiosk\");" +
                    "node(area.searchArea)[\"leisure\"](if: count_tags() > 0);" +
                    "node(area.searchArea)[\"tourism\"](if: count_tags() > 0);" +
                    "node(area.searchArea)[\"seamark:type\"](if: count_tags() > 0);" +
                    "node(area.searchArea)[\"public_transport\"](if: count_tags() > 0);" +
                    ");" +
                    "out;",
            filterNotQuery,
        )

        assertEquals(
            "[out:json][timeout:120];" +
                    "area[\"name\"=\"Jihovýchod\"]->.searchArea;" +
                    "(" +
                    "node(area.searchArea)[\"leisure\"](if: count_tags() > 0);" +
                    "node(area.searchArea)[\"tourism\"~\"hotel\"](if: count_tags() > 0);" +
                    ");" +
                    "out;",
            filterQuery,
        )
    }
}
