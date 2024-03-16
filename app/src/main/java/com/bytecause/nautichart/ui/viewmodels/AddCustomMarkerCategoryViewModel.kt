package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.repository.CustomPoiDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AddCustomMarkerCategoryViewModel @Inject constructor(
    private val repository: CustomPoiDatabaseRepository
): ViewModel() {

    suspend fun insertCategory(category: CustomPoiCategoryEntity) = repository.insertCategory(category)

    fun isCategoryNamePresent(name: String): Flow<Boolean> =
        repository.isCategoryNamePresent(name)
}