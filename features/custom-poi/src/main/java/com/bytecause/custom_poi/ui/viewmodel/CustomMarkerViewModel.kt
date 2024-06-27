package com.bytecause.custom_poi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.custom_poi.ui.ItemType
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.data.repository.CustomPoiRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomMarkerViewModel @Inject constructor(
    private val repository: CustomPoiRepositoryImpl
) : ViewModel() {

    private val getAllCategories = repository.getAllCategories()

    @OptIn(ExperimentalCoroutinesApi::class)
    val poiCategoriesFlow: StateFlow<List<ItemType>> =
        getAllCategories.mapLatest { categoryList ->
            if (categoryList.size < 20) {
                categoryList.map { ItemType.Category(it) } + ItemType.AddButton
            } else {
                categoryList.map { ItemType.Category(it) }
            }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )

    private val _poiMarkersFlow = MutableStateFlow(emptyList<CustomPoiEntity>())
    val poiMarkersFlow: StateFlow<List<CustomPoiEntity>> = _poiMarkersFlow.asStateFlow()

    var isMarkerDescriptionVisible = false
        private set

    private val _selectedItemViewPosition = MutableStateFlow(0)
    val selectedItemViewPosition: StateFlow<Int> = _selectedItemViewPosition.asStateFlow()

    fun insertCustomPoi(entity: CustomPoiEntity) {
        viewModelScope.launch {
            repository.insertCustomPoi(entity)
        }
    }

    fun getCategoryWithPois(categoryName: String) {
        viewModelScope.launch {
            repository.getCategoryWithPois(categoryName).firstOrNull()?.pois?.let { markerList ->
                _poiMarkersFlow.update {
                    markerList
                }
            }
        }
    }

    fun removePoiCategory(index: Int) {
        viewModelScope.launch {
            setSelectedItemViewPosition(RecyclerView.NO_POSITION)

            // If element is ItemType.Category delete this element from database
            (poiCategoriesFlow.value[index] as? ItemType.Category)?.category?.let {
                removeCategory(it)
            }

            // Filter out all custom markers which are under category which will be deleted
            _poiMarkersFlow.update {
                _poiMarkersFlow.value.filterNot {
                    it.categoryName == (poiCategoriesFlow.value[index] as? ItemType.Category)?.category?.categoryName
                }
            }
        }
    }

    fun removePoiMarker(markerId: Long, position: Int) {
        removeCustomPoi(markerId)

        _poiMarkersFlow.update {
            _poiMarkersFlow.value.toMutableList().apply {
                removeAt(position)
            }
        }
    }

    fun clearMarkerList() {
        _poiMarkersFlow.update {
            emptyList()
        }
    }

    private fun removeCategory(category: CustomPoiCategoryEntity) {
        viewModelScope.launch {
            repository.removeCategory(category)
        }
    }

    private fun removeCustomPoi(id: Long) {
        viewModelScope.launch {
            repository.removeCustomPoi(id)
        }
    }

    fun isMarkerDescriptionVisible(boolean: Boolean) {
        isMarkerDescriptionVisible = boolean
    }

    fun setSelectedItemViewPosition(position: Int) {
        _selectedItemViewPosition.update {
            position
        }
    }
}