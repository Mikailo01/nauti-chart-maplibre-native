package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.data.local.room.tables.relations.CategoryWithCustomPois
import com.bytecause.nautichart.data.repository.CustomPoiDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomMarkerViewModel @Inject constructor(
    private val repository: CustomPoiDatabaseRepository
) : ViewModel() {

    private val poiCategoriesList = mutableListOf<CustomPoiCategoryEntity>()
    private val poiList = mutableListOf<CustomPoiEntity>()

    var isMarkerDescriptionVisible = false
        private set

    var removeIndex: Int? = null
        private set

    var itemViewPosition: Int = 0
        private set

    fun insertCustomPoi(entity: CustomPoiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCustomPoi(entity)
        }
    }

    fun getCategoryWithPois(categoryName: String): Flow<CategoryWithCustomPois> =
        repository.getCategoryWithPois(categoryName)

    suspend fun insertCategory(category: CustomPoiCategoryEntity) =
        repository.insertCategory(category)

    val getAllCategories = repository.getAllCategories()

    fun getPoisByCategoryName(categoryName: String) = repository.getPoisByCategoryName(categoryName)

    suspend fun removeCategory(category: CustomPoiCategoryEntity) =
        repository.removeCategory(category)

    suspend fun removeCustomPoi(id: Long) = repository.removeCustomPoi(id)

    fun isMarkerDescriptionVisible(boolean: Boolean) {
        this.isMarkerDescriptionVisible = boolean
    }

    fun setItemViewPosition(position: Int) {
        this.itemViewPosition = position
    }

    fun setRemoveIndex(index: Int) {
        this.removeIndex = index
    }


}