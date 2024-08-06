package com.bytecause.domain.model

// search types to distinguish what type to search in query
sealed class SearchTypes {

    // e.x.: [amenity=bar], [amenity=restaurant], ...
    data class Amenity(val value: Array<String>) : SearchTypes() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Amenity

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    // selects all elements that have a tag with a certain key and an arbitrary value.
    // e.x.: [seamark:type], [public_transport], ...
    data class UnionSet(val value: Array<String>) : SearchTypes() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnionSet

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }
}