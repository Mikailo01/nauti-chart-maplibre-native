package com.bytecause.nautichart.domain.model

sealed class ArgsObjectTypeArray {

    data class StringType(val value: String): ArgsObjectTypeArray()
    data class IntType(val value: Int): ArgsObjectTypeArray()

    data class StringTypeArray(val value: Array<String>) : ArgsObjectTypeArray() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringTypeArray

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    data class IntTypeArray(val value: IntArray) : ArgsObjectTypeArray() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IntTypeArray

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }
}