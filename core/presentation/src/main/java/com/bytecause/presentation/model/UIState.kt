package com.bytecause.presentation.model

import com.bytecause.domain.model.Loading

data class UiState<T>(
    val loading: Loading = Loading(),
    val error: Throwable? = null,
    val items: List<T> = emptyList()
)
