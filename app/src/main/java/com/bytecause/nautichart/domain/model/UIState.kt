package com.bytecause.nautichart.domain.model

data class UiState<T>(
    val isLoading: Boolean = false,
    val error: Error? = null,
    val items: List<T> = listOf()
) {

    sealed class Error {
        data object NetworkError : Error()
        data object ServiceUnavailable : Error()
        data object Other : Error()
    }
}
