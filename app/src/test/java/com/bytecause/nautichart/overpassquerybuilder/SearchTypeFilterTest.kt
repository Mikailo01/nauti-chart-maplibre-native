package com.bytecause.nautichart.overpassquerybuilder

import com.bytecause.domain.util.SearchTypes
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTypeFilterTest {

    @Test
    fun `filter UnionSet with single key and single filter`() {
        val filteredUnionSetList: String = SearchTypes.UnionSet(listOf("tourism"))
            .filter(listOf("motel")).list.joinToString()

        assertEquals(
            "\"tourism\"~\"motel\"",
            filteredUnionSetList
        )
    }

    @Test
    fun `filter UnionSet with single key and multiple filters`() {
        val filteredUnionSetList: String = SearchTypes.UnionSet(listOf("tourism"))
            .filter(listOf("motel", "hotel", "guest_house")).list.joinToString()

        assertEquals(
            "\"tourism\"~\"motel|hotel|guest_house\"",
            filteredUnionSetList
        )
    }

    @Test
    fun `filter UnionSet with multiple keys and single filter`() {
        val filteredUnionSetList: String = SearchTypes.UnionSet(listOf("tourism", "leisure"))
            .filter(listOf("motel"), emptyList()).list.joinToString()

        assertEquals(
            "\"tourism\"~\"motel\", \"leisure\"",
            filteredUnionSetList
        )
    }

    @Test
    fun `filter UnionSet with multiple keys and multiple filters`() {
        val filteredUnionSetList: String = SearchTypes.UnionSet(listOf("tourism", "leisure"))
            .filter(listOf("motel", "guest_house"), listOf("picnic_table", "park")).list.joinToString()

        assertEquals(
            "\"tourism\"~\"motel|guest_house\", \"leisure\"~\"picnic_table|park\"",
            filteredUnionSetList
        )
    }
}