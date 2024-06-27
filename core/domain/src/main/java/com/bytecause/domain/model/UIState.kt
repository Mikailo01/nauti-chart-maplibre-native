package com.bytecause.domain.model

data class UiState<T>(
    val isLoading: Boolean = false,
    val error: Exception? = null,
    val items: List<T> = listOf()
)
