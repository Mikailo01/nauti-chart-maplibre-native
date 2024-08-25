package com.bytecause.domain.model

data class UiState<T>(
    val loading: Loading = Loading(),
    val error: Throwable? = null,
    val items: List<T> = emptyList()
)
