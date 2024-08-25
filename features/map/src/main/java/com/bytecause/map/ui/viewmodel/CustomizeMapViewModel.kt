package com.bytecause.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.UserPreferencesRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.UiState
import com.bytecause.domain.usecase.UpdateHarboursUseCase
import com.bytecause.domain.usecase.UpdateVesselsUseCase
import com.bytecause.map.ui.model.PoiCategory
import com.bytecause.util.poi.PoiUtil.getCategoriesUnderUnifiedCategory
import com.bytecause.util.poi.PoiUtil.getUnifiedPoiCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeMapViewModel
@Inject
constructor(
    private val poiCacheRepository: PoiCacheRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val updateVesselsUseCase: UpdateVesselsUseCase,
    private val updateHarboursUseCase: UpdateHarboursUseCase
) : ViewModel() {
    private val _vesselsFetchingState = MutableStateFlow<UiState<Unit>?>(null)
    val vesselsFetchingState = _vesselsFetchingState.asStateFlow()

    private val _harboursFetchingState = MutableStateFlow<UiState<Unit>?>(null)
    val harboursFetchingState = _harboursFetchingState.asStateFlow()

    private var selectedCategories: Set<String> = emptySet()

    val isAisActivated: StateFlow<Boolean> = userPreferencesRepository.getIsAisActivated()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val areHarboursVisible: StateFlow<Boolean> = userPreferencesRepository.getAreHarboursVisible()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleAisActivation() {
        viewModelScope.launch {
            userPreferencesRepository.saveIsAisActivated(!isAisActivated.value)
        }
    }

    fun toggleHarboursVisible() {
        viewModelScope.launch {
            userPreferencesRepository.saveAreHarboursVisible(!areHarboursVisible.value)
        }
    }

    val getAllDistinctCategories: Flow<List<PoiCategory>> = combine(
        poiCacheRepository.getAllDistinctCategories(),
        userPreferencesRepository.getSelectedPoiCategories()
    ) { categories, selectedCategories ->

        if (selectedCategories != null) {
            this.selectedCategories = selectedCategories
        }

        categories.map { category ->
            val categoryName = getUnifiedPoiCategory(category) ?: category

            val poiCategory = when (categoryName) {
                is Int -> {
                    PoiCategory.PoiCategoryWithNameRes(
                        nameRes = categoryName,
                        isSelected = // filter elements that are present in the database and then check if all
                        // objects under this category are present in selectedCategories set
                        getCategoriesUnderUnifiedCategory(categoryName)?.filter {
                            categories.contains(
                                it
                            )
                        }?.all {
                            selectedCategories?.contains(it) == true
                        } ?: false
                    )
                }

                else -> {
                    PoiCategory.PoiCategoryWithName(
                        name = categoryName as String,
                        isSelected = selectedCategories?.contains(category) == true
                    )
                }
            }

            poiCategory
        }.distinct()
    }

    fun selectAllCategories() {
        viewModelScope.launch {
            poiCacheRepository.getAllDistinctCategories().firstOrNull()?.let { categories ->
                selectedCategories = categories.toSet()
                userPreferencesRepository.saveSelectedPoiCategories(selectedCategories)
            }
        }
    }

    fun unselectAllCategories() {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedPoiCategories(emptySet())
        }
    }

    fun setSelectedCategory(category: PoiCategory) {
        when (category) {
            is PoiCategory.PoiCategoryWithNameRes -> {
                getCategoriesUnderUnifiedCategory(category.nameRes)?.let { categories ->
                    categories.forEach {
                        selectedCategories += it
                    }
                }
            }

            is PoiCategory.PoiCategoryWithName -> {
                selectedCategories += category.name
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.saveSelectedPoiCategories(selectedCategories)
        }
    }

    fun removeSelectedCategory(category: PoiCategory) {
        when (category) {
            is PoiCategory.PoiCategoryWithName -> {
                selectedCategories -= category.name
            }

            is PoiCategory.PoiCategoryWithNameRes -> {
                getCategoriesUnderUnifiedCategory(category.nameRes)?.let { categories ->
                    categories.forEach {
                        selectedCategories -= it
                    }
                }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.saveSelectedPoiCategories(selectedCategories)
        }
    }

    fun fetchVessels() {
        viewModelScope.launch {
            _vesselsFetchingState.emit(UiState(loading = Loading(true)))

            when (val result = updateVesselsUseCase().firstOrNull()) {
                is ApiResult.Success -> {
                    _vesselsFetchingState.emit(UiState(loading = Loading(false)))
                }

                is ApiResult.Failure -> {
                    _vesselsFetchingState.emit(
                        UiState(
                            loading = Loading(false),
                            error = result.exception
                        )
                    )
                }

                else -> _vesselsFetchingState.emit(UiState(loading = Loading(false)))
            }
        }
    }

    fun fetchHarbours() {
        viewModelScope.launch {
            _harboursFetchingState.emit(UiState(loading = Loading(true)))

            updateHarboursUseCase().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _harboursFetchingState.emit(UiState(loading = Loading(false)))
                    }

                    is ApiResult.Failure -> {
                        _harboursFetchingState.emit(
                            UiState(
                                loading = Loading(false),
                                error = result.exception
                            )
                        )
                    }

                    is ApiResult.Progress -> {
                        result.progress?.let { progress ->
                            _harboursFetchingState.emit(
                                UiState(
                                    loading = Loading(
                                        isLoading = true,
                                        progress = harboursFetchingState.value?.loading?.progress?.plus(
                                            progress
                                        ) ?: progress
                                    ),
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
