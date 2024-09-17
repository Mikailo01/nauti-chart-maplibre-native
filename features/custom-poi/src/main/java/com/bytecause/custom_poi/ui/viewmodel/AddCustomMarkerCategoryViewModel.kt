package com.bytecause.custom_poi.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.repository.CustomPoiRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AddCustomMarkerCategoryViewModel @Inject constructor(
    private val repository: CustomPoiRepositoryImpl
) : ViewModel() {

    suspend fun insertCategory(category: CustomPoiCategoryEntity) =
        repository.insertCategory(category)

    fun isCategoryNamePresent(name: String): Flow<Boolean> = repository.isCategoryNamePresent(name)
}